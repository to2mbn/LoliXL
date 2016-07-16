package org.to2mbn.lolixl.ui.service.impl;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.layout.Pane;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Service;
import org.to2mbn.lolixl.ui.service.DisplayPanesListener;
import org.to2mbn.lolixl.ui.service.DisplayService;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Component
@Service({DisplayService.class})
public class DisplayServiceImpl implements DisplayService {
	private static final Comparator<ContentPaneWrapper> ORDERED_COMPARATOR = (pre, next) -> pre.getId() - next.getId();

	private final ObservableList<ContentPaneWrapper> wrappers = FXCollections.observableArrayList();
	private final List<DisplayPanesListener> listeners = Collections.synchronizedList(new ArrayList<>());
	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

	@Override
	public List<Pane> getAvailablePanes() {
		Lock lock = rwLock.writeLock();
		lock.lock();
		try {
			return wrappers.stream()
					.map(wrapper -> wrapper.getPane())
					.collect(Collectors.toList());
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void displayPane(Pane pane) {
		Objects.requireNonNull(pane);
		Lock lock = rwLock.writeLock();
		lock.lock();
		try {
			displayPaneImpl(pane);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean closeCurrentPane() {
		Lock lock = rwLock.writeLock();
		lock.lock();
		try {
			return closeCurrentPaneImpl();
		} finally {
			lock.unlock();
		}
	}

	@Override
	public void addListener(DisplayPanesListener listener) {
		Objects.requireNonNull(listener);
		listeners.add(listener);
	}

	@Override
	public void removeListener(DisplayPanesListener listener) {
		Objects.requireNonNull(listener);
		listeners.remove(listener);
	}

	@Deactivate
	public void deactive() {
		listeners.removeAll(listeners);

		Lock lock = rwLock.writeLock();
		lock.lock();
		try {
			wrappers.removeAll(wrappers);
		} finally {
			lock.unlock();
		}
	}

	private void displayPaneImpl(Pane pane) {
		ContentPaneWrapper wrapper = new ContentPaneWrapper(pane);
		wrappers.add(wrapper);
		listeners.forEach(listener -> listener.onPaneAdded(pane));
	}

	private boolean closeCurrentPaneImpl() {
		if (wrappers.isEmpty()) {
			return false;
		}
		SortedList<ContentPaneWrapper> wrappersInOrder = wrappers.sorted(ORDERED_COMPARATOR);
		ContentPaneWrapper last = wrappersInOrder.get(wrappersInOrder.size() - 1);
		wrappers.remove(last);

		Pane previous = null;
		if (!wrappers.isEmpty()) {
			previous = wrappersInOrder.get(wrappersInOrder.size() - 2).getPane();
		}

		final Pane _previous = previous; // :(
		listeners.forEach(listener -> listener.onPaneRemoved(last.getPane(), _previous));
		return true;
	}

	private static class ContentPaneWrapper {
		private static final AtomicInteger idGenerator = new AtomicInteger(-1);

		private final Pane pane;
		private final int id;

		public ContentPaneWrapper(Pane _pane) {
			pane = _pane;
			id = idGenerator.addAndGet(1);
		}

		public Pane getPane() {
			return pane;
		}

		public int getId() {
			return id;
		}

		@Override
		public String toString() {
			return "ContentPaneWrapper{" +
					"pane=" + pane +
					", id=" + id +
					'}';
		}
	}
}
