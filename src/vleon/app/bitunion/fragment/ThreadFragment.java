package vleon.app.bitunion.fragment;

import java.util.ArrayList;

import vleon.app.bitunion.MainActivity;
import vleon.app.bitunion.PostActivity;
import vleon.app.bitunion.R;
import vleon.app.bitunion.api.BuAPI;
import vleon.app.bitunion.api.BuAPI.Result;
import vleon.app.bitunion.api.BuThread;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ThreadFragment extends SherlockListFragment {
	private ArrayList<BuThread> mData;
	private ThreadsAdapter mAdapter;
	private static int mFrom;
	final int STEP = 20;
	ActionMode mActionMode;
	int mActionItemPosition = -1;
	ProgressBar progressBar = null;
	// ���Ƽ���
	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();

	public static ThreadFragment newInstance(int fid) {
		ThreadFragment fragment = new ThreadFragment();
		mFrom = 0;
		Bundle args = new Bundle();
		args.putInt("fid", fid);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		// ���Ƽ���
		StrictMode.setThreadPolicy(policy);
		// ��fragment��ʹ��ѡ��˵�
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return inflater.inflate(R.layout.thread_fragment, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		progressBar = (ProgressBar) getView().findViewById(R.id.progressBar1);
		final ActionMode.Callback mActionCallback = new ActionMode.Callback() {

			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

				return false;
			}

			@Override
			public void onDestroyActionMode(ActionMode mode) {
				mActionMode = null;
			}

			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				MenuInflater inflater = mode.getMenuInflater();
				inflater.inflate(R.menu.thread_context_menu, menu);
				return true;
			}

			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				String string = (String) mode.getTitle();
				switch (item.getItemId()) {
				case R.id.menu_hide:
					Toast.makeText(getActivity(),
							"����: " + mData.get(mActionItemPosition).subject,
							Toast.LENGTH_SHORT).show();
					break;
				case R.id.menu_top:
					Toast.makeText(
							getActivity(),
							"�ö�: " + mData.get(mActionItemPosition).subject
									+ string, Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}
				mActionMode.finish();
				return true;
			}
		};
		/*
		 * �����¼�����ʱ�����������true��onListItemClick�����¼�Ҳ�ᴥ��, �����һֱ�ַ��¼�ֱ����������
		 */
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				mActionItemPosition = arg2;
				if (mActionMode != null) {
					return true;
				}
				mActionMode = getSherlockActivity().startActionMode(
						mActionCallback);
				return true;
			}
		});

		mData = new ArrayList<BuThread>();
		mAdapter = new ThreadsAdapter(getActivity(), mData);
		setListAdapter(mAdapter);
		fetch();
	}

	@Override
	public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu,
			MenuInflater inflater) {
		inflater.inflate(R.menu.thread, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_post:
			final View view = LayoutInflater.from(getSherlockActivity())
					.inflate(R.layout.newthread_dialog, null);
			new AlertDialog.Builder(getSherlockActivity()).setView(view)
					.setTitle("��������")
					.setNegativeButton("ȡ��", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					}).setPositiveButton("����", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							MainActivity.api.postThread(
									getArguments().getInt("fid"),
									((EditText) view
											.findViewById(R.id.newSubjectText))
											.getText().toString(),
									((EditText) view
											.findViewById(R.id.newContentText))
											.getText().toString());
							fetch();
						}
					}).show();
		case R.id.menu_refresh:
			fetch();
			break;
		case R.id.menu_next:
			fetchNextPage();
			break;
		case R.id.menu_prev:
			fetchPrevPage();
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		// TODO Auto-generated method stub
		super.onListItemClick(l, v, position, id);
		Intent intent = new Intent(getActivity(), PostActivity.class);
		BuThread thread = mData.get(position);
		intent.putExtra("id", thread.tid);
		intent.putExtra("subject", thread.subject);
		startActivity(intent);
	}

	public void fetch() {
		new FetchThreadsTask().execute();
	}

	public class FetchThreadsTask extends AsyncTask<Void, Void, Result> {
		ArrayList<BuThread> threads = new ArrayList<BuThread>();

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			progressBar.setVisibility(View.VISIBLE);
			threads.clear();
		}

		@Override
		protected Result doInBackground(Void... params) {
			int fid = ThreadFragment.this.getArguments().getInt("fid");
			return MainActivity.api.getThreads(threads, fid, mFrom, mFrom
					+ STEP);
		}

		@Override
		protected void onPostExecute(Result result) {
			progressBar.setVisibility(View.GONE);
			switch (result) {
			case SUCCESS:
				mData.clear();
				for (int i = 0; i < threads.size(); i++) {
					mData.add(threads.get(i));
				}
				mAdapter.notifyDataSetChanged();
				// �Զ�������������ʾ
				ThreadFragment.this.setSelection(0);
				break;
			case SUCCESS_EMPTY:
				Toast.makeText(getActivity(), "û������", Toast.LENGTH_SHORT)
						.show();
				break;
			case FAILURE:
				// ��������result�ֶ�Ϊfailure��ˢ��api�����»�ȡsession��һ������µڶ��λ�����ȷ����
				// �����������ԭ��һֱ�ò������ݣ���������һֱ���У�����������������Դ���
				MainActivity.api.refresh();
				fetch();
				break;
			case NETWRONG:
				Toast.makeText(getActivity(), "�������", Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				Toast.makeText(getActivity(), "δ֪����", Toast.LENGTH_SHORT)
						.show();
				break;
			}
			// switch (MainActivity.api.getError()) {
			// case BuAPI.SESSIONERROR:
			// if (MainActivity.api.apiLogin() == Result.SUCCESS) {
			// Toast.makeText(getActivity(), "���»�ȡSESSION�ɹ�",
			// Toast.LENGTH_SHORT).show();
			// fetch();
			// }
			// break;
			// case BuAPI.NONE:
			// if (threads != null) {
			// mData.clear();
			// for (int i = 0; i < threads.size(); i++) {
			// mData.add(threads.get(i));
			// }
			// // rearrange();
			// mAdapter.notifyDataSetChanged();
			// // �Զ�������������ʾ
			// ThreadFragment.this.setSelection(0);
			// }
			// break;
			// case BuAPI.NETERROR:
			// Toast.makeText(getActivity(), "�������", Toast.LENGTH_SHORT)
			// .show();
			// break;
			// default:
			// Toast.makeText(getActivity(), "δ֪����", Toast.LENGTH_SHORT)
			// .show();
			// break;
			// }
		}
	}

	class ThreadsAdapter extends BaseAdapter {
		Context context;
		ArrayList<BuThread> data;

		public ThreadsAdapter(Context context, ArrayList<BuThread> data) {
			this.context = context;
			this.data = data;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return data.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return data.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		class ViewHolder {
			TextView flagView, subjectView, authorView, countsView,
					lastpostView;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			// @SuppressWarnings("unchecked")
			BuThread item = (BuThread) getItem(position);
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.thread_item, null);
				holder = new ViewHolder();
				holder.flagView = (TextView) convertView
						.findViewById(R.id.flagText);
				holder.subjectView = (TextView) convertView
						.findViewById(R.id.subjectText);
				holder.authorView = (TextView) convertView
						.findViewById(R.id.authorText);
				holder.countsView = (TextView) convertView
						.findViewById(R.id.countText);
				holder.lastpostView = (TextView) convertView
						.findViewById(R.id.timeText);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.subjectView.setText(Html.fromHtml(item.subject));
			holder.authorView.setText(item.author);
			holder.countsView.setText(item.replies + "/" + item.views);
			holder.lastpostView.setText(item.lastpost);
			if (item.topFlag) {
				holder.flagView.setVisibility(View.VISIBLE);
			} else {
				holder.flagView.setVisibility(View.GONE);
			}
			// if (position % 2 == 0) {
			// convertView.setBackgroundResource(R.drawable.odd_item);
			// } else {
			// convertView.setBackgroundResource(R.drawable.even_item);
			// }
			return convertView;
		}
	}

	public void fetchNextPage() {
		mFrom += STEP;
		fetch();
	}

	public void fetchPrevPage() {
		mFrom -= STEP;
		if (mFrom <= 0)
			mFrom = 0;
		fetch();
	}

}