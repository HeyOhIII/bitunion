package vleon.app.bitunion;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;

import vleon.app.bitunion.api.BuAPI;
import vleon.app.bitunion.api.BuAPI.Result;
import vleon.app.bitunion.api.BuPost;
import vleon.app.bitunion.fragment.ThreadFragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PostActivity extends Activity {
	private String mTid;
	private String mSubject;
	ArrayList<BuPost> data = new ArrayList<BuPost>();
	ListAdapter adapter = null;
	ListView mListView;
	TextView mTitleView;
	int mFrom;
	final int STEP = 20;
	HashMap<String, SoftReference<Drawable>> mDrawableCache;

	// ���Ƽ���
	StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_posts);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		mTid = getIntent().getStringExtra("id");
		mSubject = getIntent().getStringExtra("subject");
		mListView = (ListView) findViewById(R.id.post_list);
		mTitleView = (TextView) findViewById(R.id.title_text);
		mTitleView.setText(Html.fromHtml(mSubject));
		adapter = new ListAdapter(this, data);
		mListView.setAdapter(adapter);
		mFrom = 0;
		mDrawableCache = new HashMap<String, SoftReference<Drawable>>();
		// ���Ƽ���
		StrictMode.setThreadPolicy(policy);
		fetchPosts();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.thread, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.menu_post).setIcon(R.drawable.social_reply);
		menu.findItem(R.id.menu_post).setTitle("�ظ�");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		case R.id.menu_refresh:
			fetchPosts();
			break;
		case R.id.menu_next:
			mFrom += STEP;
			fetchPosts();
			break;
		case R.id.menu_prev:
			mFrom -= STEP;
			if (mFrom <= 0)
				mFrom = 0;
			fetchPosts();
			break;
		case R.id.menu_post:
			// MainActivity.api.replyThread(mTid,
			// "test Bitunion Android Version");
			final View view = LayoutInflater.from(this).inflate(
					R.layout.reply_dialog, null);
			new AlertDialog.Builder(this).setView(view).setTitle(mSubject)
					.setNegativeButton("ȡ��", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					}).setPositiveButton("�ظ�", new OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							MainActivity.api.replyThread(mTid, ((EditText) view
									.findViewById(R.id.replyText)).getText()
									.toString());
							fetchPosts();
						}
					}).show();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void fetchPosts() {
		new FetchPostsTask().execute();
	}

	class FetchPostsTask extends AsyncTask<Void, Void, Result> {
		ArrayList<BuPost> posts = new ArrayList<BuPost>();
		@Override
		protected void onPreExecute() {
			// pBar.setVisibility(View.VISIBLE);
			super.onPreExecute();
			posts.clear();
		}

		@Override
		protected Result doInBackground(Void... params) {
			return MainActivity.api.getPosts(posts, mTid, mFrom, mFrom + STEP);
		}

		@Override
		protected void onPostExecute(Result result) {
			switch (result) {
			case SUCCESS:
				data.clear();
				for (int i = 0; i < posts.size(); i++) {
					data.add(posts.get(i));
				}
				adapter.notifyDataSetChanged();
				// �Զ�������������ʾ
				mListView.setSelection(0);
				break;
			case SUCCESS_EMPTY:
				Toast.makeText(PostActivity.this, "û������", Toast.LENGTH_SHORT)
						.show();
				break;
			case FAILURE:
				// ��������result�ֶ�Ϊfailure��ˢ��api�����»�ȡsession��һ������µڶ��λ�����ȷ����
				// �����������ԭ��һֱ�ò������ݣ���������һֱ���У�����������������Դ���
				MainActivity.api.refresh();
				fetchPosts();
				Toast.makeText(PostActivity.this, "���»�ȡSESSION�ɹ�",
						Toast.LENGTH_SHORT).show();
				break;
			case NETWRONG:
				Toast.makeText(PostActivity.this, "�������", Toast.LENGTH_SHORT)
						.show();
				break;
			default:
				Toast.makeText(PostActivity.this, "δ֪����", Toast.LENGTH_SHORT)
						.show();
				break;
			}
//			
//			switch (MainActivity.api.getError()) {
//			case BuAPI.SESSIONERROR:
//				if (MainActivity.api.apiLogin() == Result.SUCCESS) {
//					Toast.makeText(PostActivity.this, "���»�ȡSESSION�ɹ�",
//							Toast.LENGTH_SHORT).show();
//					fetchPosts();
//				}
//				break;
//			case BuAPI.NONE:
//				if (posts != null) {
//					data.clear();
//					for (int i = 0; i < posts.size(); i++) {
//						data.add((BuPost) posts.get(i));
//					}
//					// rearrange();
//					adapter.notifyDataSetChanged();
//					// �Զ�������������ʾ
//					mListView.setSelection(0);
//				}
//				break;
//			case BuAPI.NETERROR:
//				Toast.makeText(PostActivity.this, "�������", Toast.LENGTH_SHORT)
//						.show();
//				break;
//			default:
//				Toast.makeText(PostActivity.this, "δ֪����", Toast.LENGTH_SHORT)
//						.show();
//				break;
//			}
		}
	}

	class ListAdapter extends BaseAdapter {
		Context context;
		ArrayList<BuPost> data;
		ViewHolder holder;

		public ListAdapter(Context context, ArrayList<BuPost> data) {
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
			TextView titleView, messageView, authorView, lasteditView,
					floorView, quoteAuthorView, quoteTimeView,
					quoteContentView;
			ImageView attachmentView;
			RelativeLayout quoteLayout;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			// @SuppressWarnings("unchecked")
			BuPost item = (BuPost) getItem(position);
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.post_item, null);
				holder = new ViewHolder();
				// holder.floorView = (TextView) convertView
				// .findViewById(R.id.floorText);
				holder.attachmentView = (ImageView) convertView
						.findViewById(R.id.attachmentView);
				holder.messageView = (TextView) convertView
						.findViewById(R.id.messageView);
				holder.authorView = (TextView) convertView
						.findViewById(R.id.authorText2);
				holder.lasteditView = (TextView) convertView
						.findViewById(R.id.timeText2);
				holder.quoteLayout = (RelativeLayout) convertView
						.findViewById(R.id.quoteLayout);
				holder.quoteAuthorView = (TextView) convertView
						.findViewById(R.id.quoteAuthorView);
				holder.quoteTimeView = (TextView) convertView
						.findViewById(R.id.quoteTimeView);
				holder.quoteContentView = (TextView) convertView
						.findViewById(R.id.quoteContentView);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (item.quoteAuthor != null) {
				holder.quoteLayout.setVisibility(View.VISIBLE);
				holder.quoteAuthorView.setText(item.quoteAuthor);
				holder.quoteTimeView.setText(item.quoteTime);
				holder.quoteContentView.setText(Html.fromHtml(
						item.quoteContent, new ImageGetterFirst(
								holder.quoteContentView, item.quoteContent),
						null));
			} else {
				holder.quoteLayout.setVisibility(View.GONE);
			}
			holder.messageView.setText(Html.fromHtml(item.content,
					new ImageGetterFirst(holder.messageView, item.content),
					null));
			holder.authorView.setText(item.author);
			holder.lasteditView.setText(item.lastedit);
			// ���ֻ�����˻ظ�û�з����Լ���message����ômessageView����ʾ�������ʾ�Ļ���
			// ���ö���������һ����Ŀհף������ۣ������û��message��Ҳû�����ûظ��Ļ����������ʾ
			// messageView����Ŀ���ֻ�������ߺ�ʱ���һխ����Ҳ�����ۣ�
			if (item.quoteAuthor != null && item.content == "") {
				holder.messageView.setVisibility(View.GONE);
			} else {
				holder.messageView.setVisibility(View.VISIBLE);
			}
			if (position % 2 == 0) {
				convertView.setBackgroundResource(R.drawable.odd_item);
			} else {
				convertView.setBackgroundResource(R.drawable.even_item);
			}
			return convertView;
		}

	}

	class ImageGetterFirst implements Html.ImageGetter {

		Drawable defaultDrawable = getResources().getDrawable(
				R.drawable.content_picture);
		private TextView mTextView;
		private String mContent;

		public ImageGetterFirst(TextView textView, String content) {
			mTextView = textView;
			mContent = content;
		}

		@Override
		public Drawable getDrawable(String source) {
			Drawable drawable = null;
			if (mDrawableCache.containsKey(source)) {
				drawable = mDrawableCache.get(source).get();
			} else {
				ImageDownloadData data = new ImageDownloadData(source,
						mContent, mTextView);
				new GetImageTask(data).execute();
				drawable = defaultDrawable;
			}
			if (drawable != null)
				drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight());
			return drawable;
		}

	}

	class ImageGetterSecond implements Html.ImageGetter {

		@Override
		public Drawable getDrawable(String source) {
			if (mDrawableCache.containsKey(source)) {
				return mDrawableCache.get(source).get();
			}
			return null;
		}

	}

	class ImageDownloadData {
		private String imageSource;
		private String message;
		private TextView textView;

		public ImageDownloadData(String imageSource, String message,
				TextView textView) {
			this.imageSource = imageSource;
			this.message = message;
			this.textView = textView;
		}

		public String getImageSource() {
			return imageSource;
		}

		public String getMessage() {
			return message;
		}

		public TextView getTextView() {
			return textView;
		}
	}

	class GetImageTask extends AsyncTask<String, Void, Drawable> {
		ImageDownloadData mImageDownloadData;

		public GetImageTask(ImageDownloadData data) {
			this.mImageDownloadData = data;
		}

		@Override
		protected Drawable doInBackground(String... params) {

			Drawable drawable = Drawable
					.createFromStream(
							MainActivity.api.getImageStream(mImageDownloadData
									.getImageSource()), "src");
			drawable.setBounds(0, 0, 0 + drawable.getIntrinsicWidth(),
					0 + drawable.getIntrinsicHeight());
			return drawable;

		}

		@Override
		protected void onPostExecute(Drawable drawable) {
			mDrawableCache.put(mImageDownloadData.getImageSource(),
					new SoftReference<Drawable>(drawable));
			mImageDownloadData.getTextView().setText(
					Html.fromHtml(mImageDownloadData.getMessage(),
							new ImageGetterSecond(), null));
		}
	}
}
