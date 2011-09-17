/*
 * Copyright 2011 Jonas Bengtsson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wigwamlabs.booksapp;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.wigwamlabs.util.Pair;

public class BackgroundFileOperations {
	private static final int SLEEP_MS = 30000; // 30 s

	private static void serializeObject(String path, Serializable object) {
		try {
			final FileOutputStream fileStream = new FileOutputStream(path);
			final ObjectOutputStream out = new ObjectOutputStream(fileStream);
			try {
				out.writeObject(object);
			} finally {
				out.close();
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private final DeleteFileAction mDeleteFileAction;
	private final ConcurrentLinkedQueue<Pair<String, Serializable>> mObjectsToSerialize = new ConcurrentLinkedQueue<Pair<String, Serializable>>();
	/* package */Thread mThread;

	public BackgroundFileOperations(DeleteFileAction deleteFileAction) {
		mDeleteFileAction = deleteFileAction;
	}

	public void addObjectSerialization(String path, Serializable object) {
		mObjectsToSerialize.add(Pair.create(path, object));

		startThreadIfNecessary();
	}

	/* package */void execute() {
		while (true) {
			final Pair<String, Serializable> p = mObjectsToSerialize.poll();
			if (p != null) {
				serializeObject(p.first, p.second);
				mDeleteFileAction.onFileAdded(p.first);
				continue;
			}

			if (mDeleteFileAction.execute())
				continue;

			return;
		}
	}

	private synchronized void startThreadIfNecessary() {
		if (mThread == null) {
			mThread = new Thread() {
				@Override
				public void run() {
					boolean running = true;
					while (running) {
						execute();
						try {
							sleep(SLEEP_MS);
							running = false;
						} catch (final InterruptedException e) {
						}
					}
					mThread = null;
				}
			};
			mThread.start();
		} else {
			mThread.interrupt();
		}
	}
}
