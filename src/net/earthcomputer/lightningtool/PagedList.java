package net.earthcomputer.lightningtool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A list that is used when its contents are expected to be very large, so large
 * that it may not even fit in RAM. This list works most efficiently if elements
 * are added, iterated over or otherwise accessed almost in sequence, either
 * forwards or backwards. Jumping around the list randomly is very inefficient.
 * 
 * This list only supports appending and removing at the end. It does not
 * support insertion or deletion anywhere else in the list.
 * 
 * The list stores <tt>numPages</tt> (default = 2) "pages" of memory in RAM,
 * while the rest is stored on the disk. These pages may consist of the page in
 * which the last accessed element, and <tt>numPages - 1</tt> previously
 * accessed pages. One extra page may also be allocated for temporary copying
 * purposes. With this in consideration, the <tt>pageSize</tt> field should be
 * set so that all these pages' contents will fit in RAM.
 * 
 * This class is not thread safe.
 */
public class PagedList<E extends Serializable> extends AbstractList<E> implements Serializable {

	private static final long serialVersionUID = -5766387334126911626L;

	private static int nextListId = 0;
	private static File pagedListDir;

	private final int numPages;
	private final int pageSize;
	private final int listId;

	private List<ArrayList<E>> pages = new ArrayList<>();
	private final List<Integer> pageAccessTime = new ArrayList<>();
	private int nextPageAccessTime;
	private int loadedPages;
	private int size;

	public PagedList(int pageSize) {
		this(2, pageSize);
	}

	public PagedList(int numPages, int pageSize) {
		if (numPages < 1)
			throw new IllegalArgumentException("numPages < 1");
		if (pageSize < 1)
			throw new IllegalArgumentException("pageSize < 1");
		synchronized (PagedList.class) {
			this.listId = nextListId++;
		}
		this.numPages = numPages;
		this.pageSize = pageSize;
	}

	@Override
	public void add(int index, E value) {
		if (index < 0)
			throw new IndexOutOfBoundsException(index);
		if (index > size)
			throw new IndexOutOfBoundsException("index " + index + ", size " + size);
		if (index != size)
			throw new UnsupportedOperationException("Cannot add except to the end");
		add(value);
	}

	@Override
	public boolean add(E value) {
		getOrCreatePage(size / pageSize).add(value);
		size++;
		return true;
	}

	@Override
	public E set(int index, E value) {
		if (index < 0)
			throw new IndexOutOfBoundsException(index);
		if (index >= size)
			throw new IndexOutOfBoundsException("index " + index + ", size " + size);

		return getOrLoadPage(index / pageSize).set(index % pageSize, value);
	}

	@Override
	public E get(int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException(index);
		if (index >= size)
			throw new IndexOutOfBoundsException("index " + index + ", size " + size);

		return getOrLoadPage(index / pageSize).get(index % pageSize);
	}

	@Override
	public E remove(int index) {
		if (index < 0)
			throw new IndexOutOfBoundsException(index);
		if (index >= size)
			throw new IndexOutOfBoundsException("index " + index + ", size " + size);
		if (index != size - 1)
			throw new UnsupportedOperationException("Cannot remove except from the end");
		return remove();
	}

	public E remove() {
		if (size == 0)
			throw new IndexOutOfBoundsException("Cannot remove an element when empty");
		size--;
		return getOrLoadPage((size - 1) / pageSize).remove((size - 1) % pageSize);
	}

	@Override
	public int size() {
		return size;
	}

	private ArrayList<E> getOrCreatePage(int pageId) {
		if (pageId == pages.size()) {
			ensureSpaceForNewPage();

			ArrayList<E> page = new ArrayList<>();
			pages.add(page);
			pageAccessTime.add(nextPageAccessTime++);
			loadedPages++;
			return page;
		} else {
			return getOrLoadPage(pageId);
		}
	}

	private ArrayList<E> getOrLoadPage(int pageId) {
		ArrayList<E> page = pages.get(pageId);
		pageAccessTime.set(pageId, nextPageAccessTime++);
		if (page != null) {
			return page;
		}

		ensureSpaceForNewPage();

		page = new ArrayList<>();
		loadPageFromDisk(pageId, page);
		deletePageFromDisk(pageId);
		pages.set(pageId, page);
		loadedPages++;
		return page;
	}

	private void ensureSpaceForNewPage() {
		if (loadedPages == numPages) {
			int minAccessTime = Integer.MAX_VALUE;
			int oldestLoadedPage = -1;
			for (int i = 0; i < pageAccessTime.size(); i++) {
				if (pages.get(i) != null) {
					int accessTime = pageAccessTime.get(i);
					if (accessTime < minAccessTime) {
						minAccessTime = accessTime;
						oldestLoadedPage = i;
					}
				}
			}
			savePageToDisk(oldestLoadedPage, pages.get(oldestLoadedPage));
			pages.set(oldestLoadedPage, null);
			System.gc();
			loadedPages--;
		}
	}

	// READING AND WRITING TO DISK

	private static synchronized File getTempDir() {
		if (pagedListDir == null) {
			try {
				pagedListDir = Files.createTempDirectory("pagedlists_" + UUID.randomUUID()).toFile();
			} catch (IOException e) {
				throw new RuntimeException("Exception creating paged list directory", e);
			}
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				try {
					Files.walkFileTree(pagedListDir.toPath(), new SimpleFileVisitor<Path>() {
						@Override
						public FileVisitResult visitFile(Path path, BasicFileAttributes attr) throws IOException {
							if (!attr.isDirectory())
								Files.deleteIfExists(path);
							return FileVisitResult.CONTINUE;
						}

						@Override
						public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
							if (e != null)
								throw e;
							Files.deleteIfExists(path);
							return FileVisitResult.CONTINUE;
						}
					});
				} catch (IOException e) {
					System.err.println("Exception deleting paged list directory");
					e.printStackTrace();
				}
			}));
		}
		return pagedListDir;
	}

	private File getPageFile(int pageId) {
		return new File(getTempDir(), String.format("page_%d_%d.dat", listId, pageId));
	}

	private void savePageToDisk(int pageId, ArrayList<E> page) {
		DataOutputStream dataOut = null;
		ObjectOutputStream objectOut = null;
		try {
			File file = Files.createFile(getPageFile(pageId).toPath()).toFile();
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
			dataOut = new DataOutputStream(out);
			objectOut = new ObjectOutputStream(out);

			dataOut.writeInt(page.size());
			for (E e : page) {
				objectOut.writeObject(e);
			}
		} catch (IOException e) {
			throw new RuntimeException("Exception writing page", e);
		} finally {
			try {
				if (dataOut != null)
					dataOut.close();
				if (objectOut != null)
					objectOut.close();
			} catch (IOException ignore) {
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void loadPageFromDisk(int pageId, ArrayList<E> page) {
		DataInputStream dataIn = null;
		ObjectInputStream objectIn = null;
		try {
			File file = getPageFile(pageId);
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			dataIn = new DataInputStream(in);
			objectIn = new ObjectInputStream(in);

			int size = dataIn.readInt();
			while (size < page.size())
				page.remove(page.size() - 1);

			for (int i = 0; i < page.size(); i++)
				page.set(i, (E) objectIn.readObject());
			page.ensureCapacity(size);
			for (int i = page.size(); i < size; i++)
				page.add((E) objectIn.readObject());

		} catch (IOException e) {
			throw new RuntimeException("Exception reading page", e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Corrupt page file", e);
		} finally {
			try {
				if (dataIn != null)
					dataIn.close();
				if (objectIn != null)
					objectIn.close();
			} catch (IOException ignore) {
			}
		}
	}

	private void deletePageFromDisk(int pageId) {
		try {
			Files.deleteIfExists(getPageFile(pageId).toPath());
		} catch (IOException e) {
			throw new RuntimeException("Exception deleting page file", e);
		}
	}

}
