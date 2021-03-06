package duy.phuong.handnote.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import duy.phuong.handnote.DAO.LocalStorage;
import duy.phuong.handnote.DTO.Note;
import duy.phuong.handnote.MyView.ExpandableGridView;
import duy.phuong.handnote.MyView.NotesAdapter;
import duy.phuong.handnote.R;
import duy.phuong.handnote.Support.SupportUtils;

/**
 * Created by Phuong on 26/01/2016.
 */
public class MainFragment extends BaseFragment implements NotesAdapter.AdapterListener {
    private ExpandableGridView mListNotes;
    private ArrayList<Note> mNotes;
    private LocalStorage mLocalStorage;
    private TextView mMainTextView;
    private ShowNoteListener mShowNoteListener;
    private LinearLayout mLayoutHolder;
    private int mHolderHeight;
    private NotesAdapter mAdapter;

    public interface ShowNoteListener {
        void showNote(Note note);
    }

    public void setShowNoteListener(ShowNoteListener ShowNoteListener) {
        this.mShowNoteListener = ShowNoteListener;
    }

    public MainFragment() {
        this.mLayoutRes = R.layout.fragment_main;
        mNotes = new ArrayList<>();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mHolderHeight = getArguments().getInt("TabHeight");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mMainTextView = (TextView) mFragmentView.findViewById(R.id.mainTextView);
        mListNotes = (ExpandableGridView) mFragmentView.findViewById(R.id.listNotes);
        mListNotes.setExpanded(true);
        mLayoutHolder = (LinearLayout) mFragmentView.findViewById(R.id.layoutHolder);

    }

    @Override
    public void onStart() {
        super.onStart();
        mLocalStorage = new LocalStorage(mActivity);
        mNotes = new ArrayList<>();
        mNotes.addAll(mLocalStorage.getListNote());
        mAdapter = new NotesAdapter(mNotes, mActivity, R.layout.item_note);
        mAdapter.setListener(this);
        mListNotes.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        checkEmptyList();
        if (!mNotes.isEmpty() && mShowNoteListener != null) {
            if (mShowNoteListener.getClass() == ViewNoteFragment.class) {
                mNotes.get(0).Focused = true;
                mShowNoteListener.showNote(mNotes.get(0));
            }
        }
        mLayoutHolder.requestLayout();
        mLayoutHolder.getLayoutParams().height = mHolderHeight;
    }

    private void checkEmptyList() {
        if (mNotes.isEmpty()) {
            mMainTextView.setVisibility(View.VISIBLE);
        } else {
            mMainTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public String fragmentIdentify() {
        return BaseFragment.MAIN_FRAGMENT;
    }

    @Override
    public void deleteNote(final Note note) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(mActivity)
                        .setTitle("Confirm delete?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                boolean focused = note.Focused;
                                int index = mNotes.indexOf(note);
                                mNotes.remove(note);
                                mLocalStorage.deleteNote(note);
                                SupportUtils.deleteFile(note.mBitmapPath);
                                SupportUtils.deleteFile(note.mContentPath);
                                Toast.makeText(mActivity, "Delete done", Toast.LENGTH_SHORT).show();
                                checkEmptyList();
                                if (!mNotes.isEmpty()) {
                                    if (focused) {
                                        if (mShowNoteListener != null) {
                                            if (mShowNoteListener.getClass() == ViewNoteFragment.class) {
                                                if (index == 0 || index != mNotes.size()) {
                                                    mShowNoteListener.showNote(mNotes.get(index));
                                                    mNotes.get(index).Focused = true;
                                                } else {
                                                    mShowNoteListener.showNote(mNotes.get(index - 1));
                                                    mNotes.get(index - 1).Focused = true;
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    if (mShowNoteListener != null) {
                                        if (mShowNoteListener.getClass() == ViewNoteFragment.class) {
                                            mShowNoteListener.showNote(null);
                                        }
                                    }
                                }
                                mAdapter.notifyDataSetChanged();
                            }
                        })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        builder.create().show();
    }

    @Override
    public void showNote(Note note) {
        if (mShowNoteListener != null) {
            mShowNoteListener.showNote(note);
        }
    }
}
