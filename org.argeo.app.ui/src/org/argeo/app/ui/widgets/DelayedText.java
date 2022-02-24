package org.argeo.app.ui.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * A text input which notifies changes after a delay, typically in order to
 * apply a filter.
 */
public class DelayedText {
	private final static ScheduledExecutorService scheduler;
	static {
		// create only one scheduler, in order not to exhaust threads
		scheduler = Executors.newScheduledThreadPool(0, (r) -> {
			Thread thread = new Thread(r, "Delayed text scheduler");
			// we mark threads as deamons so that the shutdown hook is triggered
			thread.setDaemon(true);
			return thread;
		});
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			scheduler.shutdown();
		}, "Shutdown delayed text scheduler"));
	}
	private final static int DEFAULT_DELAY = 800;

	private final long delay;
	private final InternalModifyListener modifyListener;
	private final Text text;
	protected List<Consumer<String>> toDos = new ArrayList<>();
	private ServerPushSession pushSession;

	private ScheduledFuture<String> lastTask;

	public DelayedText(Composite parent, int style) {
		this(parent, style, DEFAULT_DELAY);
	}

	public DelayedText(Composite parent, int style, long delayInMs) {
		this.delay = delayInMs;
		this.modifyListener = new InternalModifyListener();
		pushSession = new ServerPushSession();
		pushSession.start();
		text = new Text(parent, style);
		text.addModifyListener(modifyListener);
	}

	protected void notifyText(String txt) {
		// text.getDisplay().syncExec(()-> pushSession.start());
		for (Consumer<String> toDo : toDos) {
			text.getDisplay().syncExec(() -> toDo.accept(txt));
		}
		// text.getDisplay().syncExec(()->pushSession.stop());
	}

	public Text getText() {
		return text;
	}

	public void addListener(Consumer<String> toDo) {
		toDos.add(toDo);
	}

	private class InternalModifyListener implements ModifyListener {
		private static final long serialVersionUID = -6178431173400385005L;

		public void modifyText(ModifyEvent e) {
			String txt = text.getText();
			ScheduledFuture<String> task = scheduler.schedule(() -> {
				notifyText(txt);
				return txt;
			}, delay, TimeUnit.MILLISECONDS);
			// cancel previous task
			if (lastTask != null && !lastTask.isDone()) {
				lastTask.cancel(false);
			}
			lastTask = task;
		}
	};

}
