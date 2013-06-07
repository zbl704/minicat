package com.mcxiaoke.fanfouapp.fragment;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mcxiaoke.fanfouapp.R;
import com.mcxiaoke.fanfouapp.controller.EmptyViewController;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import uk.co.senab.photoview.PhotoView;

import java.util.ArrayList;
import java.util.List;

/**
 * Project: fanfouapp
 * Package: com.mcxiaoke.fanfouapp.fragment
 * User: mcxiaoke
 * Date: 13-6-5
 * Time: 下午9:56
 */
public class GalleryFragment extends Fragment implements ViewPager.OnPageChangeListener {

    public static GalleryFragment newInstance(ArrayList<String> data, int index) {
        GalleryFragment fragment = new GalleryFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("data", data);
        args.putInt("index", index);
        fragment.setArguments(args);
        return fragment;
    }

    private ViewPager mViewPager;
    private TextView mTextView;
    private GalleryPagerAdapter mGalleryPagerAdapter;
    private List<String> mImageUris;
    private int mIndex;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageUris = new ArrayList<String>();
        Bundle args = getArguments();
        ArrayList<String> data = args.getStringArrayList("data");
        mIndex = args.getInt("index");
        if (data != null) {
            mImageUris.addAll(data);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fm_gallery, null);
        mViewPager = (ViewPager) root.findViewById(R.id.gallery);
        mTextView = (TextView) root.findViewById(R.id.text);
        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGalleryPagerAdapter = new GalleryPagerAdapter(getActivity(), mImageUris);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setAdapter(mGalleryPagerAdapter);
        mViewPager.setCurrentItem(mIndex);
        setPageText(mIndex);
        mViewPager.setOnPageChangeListener(this);
    }

    private void setPageText(int page) {
        mTextView.setText("" + (page + 1) + " / " + mImageUris.size());
    }

    @Override
    public void onPageSelected(int position) {
        setPageText(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }


    static class GalleryPagerAdapter extends PagerAdapter {
        private Context mContext;
        private LayoutInflater mInflater;
        private List<String> mResources;

        public GalleryPagerAdapter(Context context, List<String> resources) {
            mContext = context;
            mInflater = LayoutInflater.from(context);
            mResources = resources;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
//            PhotoView view = new PhotoView(container.getContext());
//            container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            ImageLoader.getInstance().displayImage(mResources.get(position), view);

            ViewGroup view = (ViewGroup) mInflater.inflate(R.layout.gallery_item_photo, null);
            final PhotoView imageView = (PhotoView) view.findViewById(R.id.photo);
            View vEmpty = view.findViewById(android.R.id.empty);
            final EmptyViewController emptyViewController = new EmptyViewController(vEmpty);
            ImageLoader.getInstance().displayImage(mResources.get(position), imageView, getDisplayImageOptions(), new ImageLoaderCallback(imageView, emptyViewController));

            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object view) {
            container.removeView((View) view);
        }

        @Override
        public int getCount() {
            return mResources.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        private static DisplayImageOptions getDisplayImageOptions() {
            return new DisplayImageOptions.Builder().cacheOnDisc().cacheInMemory().showStubImage(R.drawable.photo_loading)
                    .build();
        }

        private class ImageLoaderCallback extends SimpleImageLoadingListener {
            private PhotoView imageView;
            private EmptyViewController emptyViewController;

            public ImageLoaderCallback(PhotoView imageView, EmptyViewController emptyViewController) {
                this.imageView = imageView;
                this.emptyViewController = emptyViewController;

            }

            private void showProgress() {
                imageView.setVisibility(View.GONE);
                emptyViewController.showProgress();
            }

            private void showEmptyText(String text) {
                imageView.setVisibility(View.GONE);
                emptyViewController.showEmpty(text);
            }

            private void showContent(Bitmap bitmap) {
                emptyViewController.hideProgress();
                imageView.setVisibility(View.VISIBLE);
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                showContent(loadedImage);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                showEmptyText(failReason.getType().toString());
            }

            @Override
            public void onLoadingStarted(String imageUri, View view) {
                showProgress();
            }
        }
    }


}