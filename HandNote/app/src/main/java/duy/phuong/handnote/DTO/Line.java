package duy.phuong.handnote.DTO;

import java.util.ArrayList;

/**
 * Created by Phuong on 09/04/2016.
 */
public class Line {
    public ArrayList<Character> mCharacters;
    public int mTop, mBottom;
    public int mMinTop, mMaxBottom;

    public Line() {
        mMinTop = mMaxBottom = 0;
    }
}
