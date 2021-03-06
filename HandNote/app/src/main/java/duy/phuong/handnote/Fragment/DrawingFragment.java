package duy.phuong.handnote.Fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.support.v7.widget.PopupMenu;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;

import duy.phuong.handnote.DTO.Character;
import duy.phuong.handnote.Listener.BackPressListener;
import duy.phuong.handnote.MyView.DrawingView.BitmapPager;
import duy.phuong.handnote.MyView.DrawingView.FingerDrawerView;
import duy.phuong.handnote.MyView.BitmapAdapter;
import duy.phuong.handnote.R;
import duy.phuong.handnote.Recognizer.BitmapProcessor;
import duy.phuong.handnote.Support.SupportUtils;

/**
 * Created by Phuong on 23/11/2015.
 */
public class DrawingFragment extends BaseFragment implements View.OnClickListener, BackPressListener, BitmapProcessor.DetectCharactersCallback{
    private GridView mListDetectedBitmap;
    private ArrayList<Bitmap> mListBitmap;
    private BitmapAdapter mBitmapAdapter;
    private ImageButton mButtonMore;
    private EditText mEdtName;
    private int mCurrentMode;

    private FingerDrawerView mDrawer;
    private PopupMenu mPopupMenu;

    private ViewPager mBitmapViewPager;
    private BitmapPager mBitmapPager;
    private ArrayList<Bitmap> mAnalysisBitmaps;

    private LinearLayout mLayoutProcessing;

    public DrawingFragment() {
        this.mLayoutRes = R.layout.fragment_drawing;
        mListBitmap = new ArrayList<>();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageButton mButtonSave = (ImageButton) mFragmentView.findViewById(R.id.buttonSave);
        mButtonSave.setOnClickListener(this);
        ImageButton mButtonEmpty = (ImageButton) mFragmentView.findViewById(R.id.buttonDelete);
        mButtonEmpty.setOnClickListener(this);
        ImageButton mButtonUndo = (ImageButton) mFragmentView.findViewById(R.id.buttonUndo);
        mButtonUndo.setOnClickListener(this);
        ImageButton mButtonRedo = (ImageButton) mFragmentView.findViewById(R.id.buttonRedo);
        mButtonRedo.setOnClickListener(this);
        ImageButton mButtonForward = (ImageButton) mFragmentView.findViewById(R.id.buttonForward);
        mButtonForward.setOnClickListener(this);
        mButtonMore = (ImageButton) mFragmentView.findViewById(R.id.buttonMore);
        mButtonMore.setOnClickListener(this);
        mEdtName = (EditText) mFragmentView.findViewById(R.id.edtName);
        mBitmapViewPager = (ViewPager) mFragmentView.findViewById(R.id.pagerImages);
        mAnalysisBitmaps = new ArrayList<>();
        mBitmapPager = new BitmapPager(mActivity, mAnalysisBitmaps, R.layout.item_bitmap_2);
        mBitmapViewPager.setAdapter(mBitmapPager);

        mLayoutProcessing = (LinearLayout) mFragmentView.findViewById(R.id.layoutProcess);

        CheckBox mCheckSplit = (CheckBox) mFragmentView.findViewById(R.id.ckcSplit);
        mCheckSplit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDrawer.setSplit();
            }
        });
        mCurrentMode = R.id.itemDefault;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListDetectedBitmap = (GridView) mFragmentView.findViewById(R.id.listDetectedBitmap);

        mBitmapAdapter = new BitmapAdapter(mActivity, R.layout.item_bitmap, mListBitmap);
        mListDetectedBitmap.setAdapter(mBitmapAdapter);

        mDrawer = (FingerDrawerView) mFragmentView.findViewById(R.id.FingerDrawer);
        mDrawer.setListener(this);
        mDrawer.setDisplayListener(this);
        mDrawer.setDefault();

        mPopupMenu = new PopupMenu(mActivity, mButtonMore);
        mPopupMenu.getMenuInflater().inflate(R.menu.menu_draw_fragment, mPopupMenu.getMenu());
        mPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.itemContour:
                        mDrawer.setFindContours();
                        break;
                    case R.id.itemVerticalProjectionProfile:
                        mDrawer.setFindVerticalProjectionProfile();
                        break;
                    case R.id.itemHorizontalProjectionProfile:
                        mDrawer.setFindHorizontalProjectionProfile();
                        break;
                    case R.id.itemProfile:
                        mDrawer.setProfile();
                        break;
                    case R.id.itemDefault:
                        mDrawer.setDefault();
                        break;
                    default:
                        break;
                }
                mCurrentMode = item.getItemId();
                item.setChecked(true);
                return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public String fragmentIdentify() {
        return DRAWING_FRAGMENT;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonSave:
                String currentName = mEdtName.getText().toString();
                for (Bitmap bitmap : mListBitmap) {
                    if (!SupportUtils.saveImage(bitmap, "Draw", currentName, ".png")) {
                        Toast.makeText(mActivity, "Save images error", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                Toast.makeText(mActivity, "Done", Toast.LENGTH_SHORT).show();
                break;
            case R.id.buttonDelete:
                emptyDrawer();
                break;
            case R.id.buttonForward:
                mListener.showFragment(BaseFragment.LEARNING_FRAGMENT);
                break;
            case R.id.buttonMore:
                mPopupMenu.show();
                break;
            case R.id.buttonUndo:
                mDrawer.undo(new FingerDrawerView.UndoRedoCallback() {
                    @Override
                    public void canUndoRedo(boolean possibility) {
                        Toast.makeText(mActivity, "Undo is aborted because the latest action isn't completed yet", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void emptyStack() {
                        mListBitmap.clear();
                        mBitmapAdapter.notifyDataSetChanged();
                    }
                });
                break;
            case R.id.buttonRedo:
                mDrawer.redo(new FingerDrawerView.UndoRedoCallback() {
                    @Override
                    public void canUndoRedo(boolean possibility) {
                        Toast.makeText(mActivity, "Redo is aborted because the latest action isn't completed yet", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void emptyStack() {
                        Toast.makeText(mActivity, "Nothing to redo", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            default:
                break;
        }
    }

    private void emptyDrawer() {
        mDrawer.emptyDrawer();
        mListBitmap.clear();
        mBitmapAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean doBack() {
        if (mListBitmap.size() > 0 || mAnalysisBitmaps.size() > 0) {
            mAnalysisBitmaps.clear();
            mBitmapPager = new BitmapPager(mActivity, mAnalysisBitmaps, R.layout.item_bitmap_2);
            mBitmapViewPager.setAdapter(mBitmapPager);
            emptyDrawer();
            return true;
        }
        return false;
    }


    @Override
    public void onBeginDetect(Bundle bundle) {
        mLayoutProcessing.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDetectSuccess(ArrayList<Character> listCharacters) {
        mListBitmap.clear();
        if (listCharacters.size() > 0) {
            switch (mCurrentMode) {
                case R.id.itemVerticalProjectionProfile:
                case R.id.itemHorizontalProjectionProfile:
                case R.id.itemContour:
                case R.id.itemProfile:
                    mListDetectedBitmap.setVisibility(View.GONE);
                    mBitmapViewPager.setVisibility(View.VISIBLE);
                    if (mAnalysisBitmaps != null) {
                        mAnalysisBitmaps.clear();
                        for (Character c : listCharacters) {
                            if (c.mBitmap != null) {
                                mAnalysisBitmaps.add(c.mBitmap);
                            }
                        }
                        mBitmapPager = new BitmapPager(mActivity, mAnalysisBitmaps, R.layout.item_bitmap_2);
                        mBitmapViewPager.setAdapter(mBitmapPager);
                    }
                    break;
                default:
                    mListDetectedBitmap.setVisibility(View.VISIBLE);
                    mBitmapViewPager.setVisibility(View.GONE);
                    for (Character character : listCharacters) {
                        mListBitmap.add(character.mBitmap);
                    }
                    break;
            }
            mLayoutProcessing.setVisibility(View.GONE);
            Toast.makeText(mActivity, "Images analysis done", Toast.LENGTH_SHORT).show();
            mBitmapAdapter.notifyDataSetChanged();
        } else {
            mLayoutProcessing.setVisibility(View.GONE);
        }
    }
}
