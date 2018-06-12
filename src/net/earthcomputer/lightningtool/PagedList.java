package net.earthcomputer.lightningtool;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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
 * <p>
 * A list that is used when its contents are expected to be very large, so large
 * that it may not even fit in RAM. This list works most efficiently if elements
 * are added, iterated over or otherwise accessed almost in sequence, either
 * forwards or backwards. Jumping around the list randomly is very inefficient.
 * </p>
 * 
 * <p>
 * This list only supports appending and removing at the end. It does not
 * support insertion or deletion anywhere else in the list.
 * </p>
 * 
 * <p>
 * The list stores <tt>numPages</tt> (default = 2) "pages" of memory in RAM,
 * while the rest is stored on the disk. These pages may consist of the page in
 * which the last accessed element, and <tt>numPages - 1</tt> previously
 * accessed pages. With this in consideration, the <tt>pageSize</tt> field
 * should be set so that all these pages' contents will fit in RAM.
 * </p>
 * 
 * <p>
 * Instances of <tt>PagedList</tt> are not thread safe.
 * </p>
 */
public class PagedList<E> extends AbstractList<E> implements Closeable {

	private static int nextListId = 0;
	private static File pagedListDir;

	private final int numPages;
	private final int pageSize;
	private final int listId;
	private final ElementSerializer<E> serializer;

	private List<ArrayList<E>> pages = new ArrayList<>();
	private final List<Integer> pageAccessTime = new ArrayList<>();
	private int nextPageAccessTime;
	private int loadedPages;
	private int size;

	public PagedList(int pageSize, ElementSerializer<E> serializer) {
		this(2, pageSize, serializer);
	}

	public PagedList(int numPages, int pageSize, ElementSerializer<E> serializer) {
		if (numPages < 1)
			throw new IllegalArgumentException("numPages < 1");
		if (pageSize < 1)
			throw new IllegalArgumentException("pageSize < 1");
		synchronized (PagedList.class) {
			this.listId = nextListId++;
		}
		this.numPages = numPages;
		this.pageSize = pageSize;
		this.serializer = serializer;
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

	@Override
	public String toString() {
		return "PagedList{pages=" + (size == 0 ? 0 : (size - 1) / pageSize + 1) + "}";
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
		System.out.println(pagedListDir);
		return pagedListDir;
	}

	private File getPageFile(int pageId) {
		return new File(getTempDir(), String.format("page_%d_%d.dat", listId, pageId));
	}

	private void savePageToDisk(int pageId, ArrayList<E> page) {
		DataOutputStream dataOut = null;
		try {
			File file = Files.createFile(getPageFile(pageId).toPath()).toFile();
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
			dataOut = new DataOutputStream(out);

			dataOut.writeInt(page.size());
			for (E e : page) {
				serializer.serialize(dataOut, e);
			}
			dataOut.flush();
		} catch (IOException e) {
			throw new RuntimeException("Exception writing page", e);
		} finally {
			try {
				if (dataOut != null)
					dataOut.close();
			} catch (IOException ignore) {
			}
		}
	}

	private void loadPageFromDisk(int pageId, ArrayList<E> page) {
		DataInputStream dataIn = null;
		try {
			File file = getPageFile(pageId);
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
			dataIn = new DataInputStream(in);

			int size = dataIn.readInt();
			if (size > pageSize)
				size = pageSize;
			while (size < page.size())
				page.remove(page.size() - 1);

			for (int i = 0; i < page.size(); i++)
				page.set(i, serializer.deserialize(dataIn));
			page.ensureCapacity(size);
			for (int i = page.size(); i < size; i++)
				page.add(serializer.deserialize(dataIn));

		} catch (IOException e) {
			throw new RuntimeException("Exception reading page", e);
		} finally {
			try {
				if (dataIn != null)
					dataIn.close();
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

	@Override
	public void close() {
		for (int i = 0; i < pages.size(); i++)
			deletePageFromDisk(i);
	}

	public static interface ElementSerializer<E> {
		void serialize(DataOutputStream out, E e) throws IOException;

		E deserialize(DataInputStream in) throws IOException;
	}

}
