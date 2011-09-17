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

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;

import com.wigwamlabs.booksapp.db.DatabaseAdapter;
import com.wigwamlabs.util.Compatibility;

public final class ExportBookAction {
	public interface Callback {
		public void onGoogleDocsActionCreated(UploadToGoogleDocsAction action);
	}

	public static void export(final Context context, final DatabaseAdapter db,
			final SubActivityManager manager,
			final AsyncTaskListener<Long, Long, Integer> listener, final Long[] ids,
			final Callback callback) {
		if (Compatibility.SDK_INT < Build.VERSION_CODES.ECLAIR) {
			// AccountManager not available until eclair, so skip Google Docs
			// export
			exportSend(manager, db, listener, ids);
		} else {
			final DialogInterface.OnClickListener menuListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface d, int pos) {
					if (pos == 0) {
						exportSend(manager, db, listener, ids);
					} else if (pos == 1) {
						exportGoogleDocs(manager, db, listener, ids, callback);
					}
				}
			};
			final ContextMenu menu = new ContextMenu(context, menuListener);
			menu.add(R.string.export_send);
			menu.add(R.string.export_google_docs);
			menu.show();
		}
	}

	/* package */static void exportGoogleDocs(SubActivityManager manager, DatabaseAdapter db,
			AsyncTaskListener<Long, Long, Integer> listener, Long[] ids, Callback callback) {
		final UploadToGoogleDocsAction action = new UploadToGoogleDocsAction(manager.getActivity(),
				db, listener, ids);
		callback.onGoogleDocsActionCreated(action);
		action.start();
	}

	/* package */static void exportSend(SubActivityManager manager, DatabaseAdapter db,
			AsyncTaskListener<Long, Long, Integer> listener, Long[] ids) {
		final ExportBooksSendTask task = ExportBooksSendTask.createOrNull(manager.getActivity(),
				db, listener);
		if (task != null)
			task.execute(ids);
	}

	private ExportBookAction() {
	}
}
