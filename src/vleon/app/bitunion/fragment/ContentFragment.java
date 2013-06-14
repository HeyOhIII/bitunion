package vleon.app.bitunion.fragment;

import vleon.app.bitunion.MainActivity;
import vleon.app.bitunion.MainAdapter;
import vleon.app.bitunion.R;
import vleon.app.bitunion.api.BuAPI.Result;
import vleon.app.bitunion.api.BuThread;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class ContentFragment extends SherlockListFragment implements
		OnScrollListener {
	public static final int THREAD = 0;
	public static final int POST = 1;
	OnContentItemClickListener mItemClickListener;
	public MainAdapter mAdapter;
	ActionMode mActionMode;
	TextView loadNextPageView;
	public int mCurrentPageCnt;
	public final int mPageStep = 20;
	int mActionItemPosition;
	private int visibleItemCount;
	private int visibleLastIndex;
	private MenuItem mRefreshItem;
	View loadMoreView;

	/*
	 * �б��������ܽӿڶ���
	 */
	public interface OnContentItemClickListener {
		public void onItemClicked(int position);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// �����������������loadmoreview����������ִ��
				if (arg2 >= mAdapter.getCount()) {
					return false;
				}
				mActionItemPosition = arg2;
				if (mActionMode != null) {
					// if already in action mode - do nothing
					return false;
				}
				mAdapter.beginSelected();
				mAdapter.addSelects(arg2);
				mAdapter.notifyDataSetChanged();
				mActionMode = getSherlockActivity().startActionMode(
						new ActionModeCallback());
				mActionMode.invalidate();
				mActionMode.setTitle("��ѡ��" + mAdapter.getSelectedCnt() + "��");
				return true;
			}
		});
		loadMoreView = LayoutInflater.from(getSherlockActivity()).inflate(
				R.layout.loadmore, null);
		loadNextPageView = (TextView) loadMoreView
				.findViewById(R.id.loadNextPageView);

		getListView().addFooterView(loadMoreView, null, true);
		getListView().setOnScrollListener(this);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mItemClickListener = (OnContentItemClickListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ "����ʵ��OnContentItemClickListener�ӿ�");
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.thread, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		mRefreshItem = menu.findItem(R.id.menu_refresh);
	}

	public void refresh() {
		mCurrentPageCnt = 0;
		mAdapter.clear();
		fetchContents();
	}

	/*
	 * ֻ��ҪPostFragment��д
	 */
	public void replyOthers() {
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_post:
			reply();
			break;
		case R.id.menu_refresh:
			refresh();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	class ActionModeCallback implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.thread_context_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			if (getThisTag() == POST) {
				menu.findItem(R.id.menu_quotereply).setVisible(true);
			}
			return true;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			switch (item.getItemId()) {
			case R.id.menu_hide:
			case R.id.menu_top:
				showToast("���ܻ�δ����!");
				break;
			case R.id.menu_quotereply:
				replyOthers();
				mode.finish();
				break;
			case R.id.menu_profile:
				int i = Integer.valueOf(mAdapter.getSelected().get(0));
				ProfileFragment fragment = ProfileFragment.newInstance(
						mAdapter.getAuthorID(i), mAdapter.getAuthor(i));
				FragmentTransaction ft = getSherlockActivity()
						.getSupportFragmentManager().beginTransaction();
				fragment.show(ft, "������Ϣ");
				mode.finish();
				break;
			default:
				break;
			}
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mActionMode = null;
			mAdapter.endSelected();
		}

	}

	public Result fetchContentTask() {
		return null;
	}

	public class FetchContentTask extends AsyncTask<Void, Void, Result> {
		int currentAdapterCount = 0;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			setRefreshActionViewState(true);
			currentAdapterCount = mAdapter.getCount();
			if (mAdapter.getCount() < mPageStep) {
				loadNextPageView.setVisibility(View.GONE);
			}else {
				loadNextPageView.setVisibility(View.VISIBLE);
			}
		}

		@Override
		protected Result doInBackground(Void... params) {

			return fetchContentTask();
		}

		@Override
		protected void onPostExecute(Result result) {
			loadNextPageView.setText("");
			if (mAdapter.getCount() - currentAdapterCount < mPageStep) {
				loadNextPageView.setVisibility(View.GONE);
			}else {
				loadNextPageView.setVisibility(View.VISIBLE);
			}
			setRefreshActionViewState(false);
			switch (result) {
			case SUCCESS:
				mAdapter.notifyDataSetChanged();
				break;
			case SUCCESS_EMPTY:
				mCurrentPageCnt -= mPageStep;
				showToast("û������");
				break;
			case FAILURE:
				showToast("��ȡsessionʧ��");
				break;
			case NETWRONG:
				showToast("�������");
				break;
			default:
				break;
			}
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		// �����������������loadmoreview���������ִ��
		if (position >= mAdapter.getCount()) {
			return;
		}
		if (mActionMode != null) {
			mAdapter.toggleSelected(position);
			mActionMode.setTitle("��ѡ��" + mAdapter.getSelectedCnt() + "��");
			if (mAdapter.getSelectedCnt() > 1) {
				mActionMode.getMenu().findItem(R.id.menu_profile)
						.setVisible(false);
			} else {
				mActionMode.getMenu().findItem(R.id.menu_profile)
						.setVisible(true);
			}
			if (mAdapter.getSelectedCnt() == 0) {
				mActionMode.finish();
			}
		} else {
			if (getThisTag() == THREAD) {
				mItemClickListener.onItemClicked(position);
			}
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		this.visibleItemCount = visibleItemCount;
		visibleLastIndex = firstVisibleItem + visibleItemCount - 1;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		int itemsLastIndex = mAdapter.getCount() - 1; // ���ݼ����һ�������
		int lastIndex = itemsLastIndex + 1; // ���ϵײ���loadMoreView��
		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
				&& visibleLastIndex == lastIndex) {
			loadNextPageView.setText("���ڼ�����һҳ");
			fetchNextPage();
		}
	}

	/*
	 * ���ظ����˵����������д
	 */
	public void reply() {

	}

	/*
	 * ��ȡҳ������
	 */
	public void fetchContents() {
		new FetchContentTask().execute();
	}

	public void fetchNextPage() {
		mCurrentPageCnt += mPageStep;
		fetchContents();
	}

	public void fetchPrevPage() {
		mCurrentPageCnt -= mPageStep;
		if (mCurrentPageCnt < 0)
			mCurrentPageCnt = 0;
		fetchContents();
	}

	void showToast(String str) {
		Toast.makeText(getSherlockActivity(), str, Toast.LENGTH_SHORT).show();
	}

	/*
	 * ��ȡ��ǰfragment����
	 */
	public int getThisTag() {
		return getArguments().getInt("tag");
	}

	/*
	 * �趨��ˢ�¡��˵����actionview
	 */
	public void setRefreshActionViewState(boolean refreshing) {
		if (mRefreshItem == null)
			return;
		if (refreshing) {
			mRefreshItem.setActionView(R.layout.progress);
		} else {
			mRefreshItem.setActionView(null);
		}
	}

}
