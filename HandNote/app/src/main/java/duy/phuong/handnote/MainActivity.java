package duy.phuong.handnote;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.PopupMenu;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import duy.phuong.handnote.DTO.ClusterLabel;
import duy.phuong.handnote.DTO.Label;
import duy.phuong.handnote.DTO.Line;
import duy.phuong.handnote.DTO.Note;
import duy.phuong.handnote.DTO.SideMenuItem;
import duy.phuong.handnote.Fragment.BaseFragment;
import duy.phuong.handnote.Fragment.CreateNoteFragment;
import duy.phuong.handnote.Fragment.TranslateFragment;
import duy.phuong.handnote.Fragment.DrawingFragment;
import duy.phuong.handnote.Fragment.LearningFragment;
import duy.phuong.handnote.Fragment.MainFragment;
import duy.phuong.handnote.Fragment.TemplatesFragment;
import duy.phuong.handnote.Fragment.ViewNoteFragment;
import duy.phuong.handnote.Fragment.WebFragment;
import duy.phuong.handnote.Listener.BackPressListener;
import duy.phuong.handnote.Listener.MainListener;
import duy.phuong.handnote.MyView.RoundImageView;
import duy.phuong.handnote.MyView.SideMenuAdapter;
import duy.phuong.handnote.Recognizer.MachineLearning.Input;
import duy.phuong.handnote.Recognizer.MachineLearning.SOM;
import duy.phuong.handnote.Support.LanguageUtils;
import duy.phuong.handnote.Support.SharedPreferenceUtils;
import duy.phuong.handnote.Support.SupportUtils;

public class MainActivity extends FragmentActivity implements MainListener, ImageButton.OnClickListener, BackPressListener,
        MainFragment.ShowNoteListener{
    private FragmentManager mFragmentManager;
    private DrawerLayout mMainNavigator;
    private LinearLayout mSideMenu;
    private FrameLayout mLayoutBottomTabs;
    private ImageButton mButtonNavigator;
    private ImageButton mButtonBack;
    private TextView mTvAppTitle, mTvUsername;
    private RoundImageView mAvatar;

    private int mTabWidth = 0, mTabHeight = 0;

    private BackPressListener mBackPressListener;
    private Stack<String> mStack;

    private SOM mGlobalSOM;
    private ArrayList<ClusterLabel> mGlobalMapNames;

    private String mFragmentName = "";

    private Note mCurrentNote;
    private SideMenuAdapter mSideMenuAdapter;
    private ListView mListSideMenu;
    private ArrayList<SideMenuItem> mItems;

    private Button mTabNotes, mTabTemplates;
    private TextView mTvInternet;

    private LinearLayout mBottomTabs;
    private ImageButton mButtonCreate;
    private LinearLayout mBorderButtonCreate;

    private PopupMenu mPopUpMenu;

    private LinearLayout mLayoutLoading;
    private HandNote mHandNote;
    private boolean mSetTabSize;
    private boolean LandScape;
    private LinearLayout mLayoutAvatar;

    private MainFragment mMainFragment;
    private LinearLayout mLayoutSubFragment;
    private int mBackClickCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandNote = (HandNote) getApplication();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(Color.parseColor("#FFCC0033"));
        }
        mLayoutLoading = (LinearLayout) findViewById(R.id.layoutLoading);
        mLayoutLoading.setVisibility(View.VISIBLE);

        mSetTabSize = false;

        mTabTemplates = (Button) findViewById(R.id.buttonTemplates);
        mTabTemplates.setOnClickListener(this);
        mTabNotes = (Button) findViewById(R.id.buttonListNotes);
        mTabNotes.setOnClickListener(this);

        mLayoutBottomTabs = (FrameLayout) findViewById(R.id.layoutTabsBottom);
        mSideMenu = (LinearLayout) findViewById(R.id.layoutSideMenu);
        mMainNavigator = (DrawerLayout) findViewById(R.id.layoutMainNavigator);
        mButtonNavigator = (ImageButton) findViewById(R.id.buttonMainNavigator);
        mButtonNavigator.setOnClickListener(this);
        mButtonCreate = (ImageButton) findViewById(R.id.buttonCreate);
        mButtonCreate.setOnClickListener(this);
        mBorderButtonCreate = (LinearLayout) findViewById(R.id.borderButtonCreate);
        mButtonBack = (ImageButton) findViewById(R.id.buttonBack);
        mButtonBack.setOnClickListener(this);
        ImageButton mButtonMenu = (ImageButton) findViewById(R.id.buttonMenu);
        mButtonMenu.setOnClickListener(this);
        mTvAppTitle = (TextView) findViewById(R.id.tvAppTitle);
        mTvUsername = (TextView) findViewById(R.id.tvUsername);
        mTvInternet = (TextView) findViewById(R.id.tvInternet);
        mAvatar = (RoundImageView) findViewById(R.id.imageAvatar);
        mListSideMenu = (ListView) findViewById(R.id.listSideMenu);
        mLayoutAvatar = (LinearLayout) findViewById(R.id.layoutAvatar);
        mLayoutSubFragment = (LinearLayout) findViewById(R.id.layoutSubFragmentContainer);

        mBottomTabs = (LinearLayout) findViewById(R.id.layoutBottomTabs);

        mStack = new Stack<>();

        mFragmentManager = getSupportFragmentManager();

        mPopUpMenu = new PopupMenu(MainActivity.this, mButtonMenu);
        mPopUpMenu.getMenuInflater().inflate(R.menu.menu_main_task, mPopUpMenu.getMenu());
        mPopUpMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.itemLogout:
                        mHandNote.deleteAllNote();
                        SharedPreferenceUtils.setCurrentName("");
                        SharedPreferenceUtils.viewedIntro(false);
                        SupportUtils.deleteAvatar();
                        Intent intent = new Intent(MainActivity.this, StartActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        initMapNames();
        initSOM();
        if (savedInstanceState != null) {
            mFragmentName = savedInstanceState.getString("Fragment");
            mSetTabSize = savedInstanceState.getBoolean("isSetSize");
            mTabWidth = savedInstanceState.getInt("tabWidth");
            mTabHeight = savedInstanceState.getInt("tabHeight");
            LandScape = savedInstanceState.getBoolean("LandScape");
            ArrayList<String> list = savedInstanceState.getStringArrayList("Stack");
            if (list != null) {
                for (int i = list.size() - 1; i >= 0; i--) {
                    mStack.push(list.get(i));
                }
            }
            mCurrentNote = (Note) savedInstanceState.getSerializable("Note");
        }
        if (mFragmentName == null || mFragmentName.length() == 0) {
            mFragmentName = BaseFragment.MAIN_FRAGMENT;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        screenOrientation(mFragmentName);
        mBackClickCount = 1;
        updateButtonAdd();
        mItems = new ArrayList<>();
        mItems.add(new SideMenuItem(R.mipmap.ic_android_black_24dp, R.mipmap.ic_android_red_24dp, getString(R.string.training_en), false));
        mItems.add(new SideMenuItem(R.mipmap.ic_translate_black_24dp, R.mipmap.ic_translate_red_24dp, getString(R.string.translate_en), false));
        mItems.add(new SideMenuItem(R.mipmap.ic_public_black_24dp, R.mipmap.ic_public_red_24dp, getString(R.string.web_en), false));
        mSideMenuAdapter = new SideMenuAdapter(mItems, MainActivity.this, R.layout.layout_side_menu);
        mListSideMenu.setAdapter(mSideMenuAdapter);
        mSideMenuAdapter.notifyDataSetChanged();
        mListSideMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AsyncTask<Void, Void, Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        toggleMainNavigator(false);
                    }

                    @Override
                    protected Void doInBackground(Void... params) {
                        long times = System.currentTimeMillis();
                        while (System.currentTimeMillis() - times < 500);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        SideMenuItem item = mItems.get(position);
                        switch (item.mTitle) {
                            case "Image Analysis":
                                final Dialog dialog = new Dialog(MainActivity.this);
                                dialog.setContentView(R.layout.layout_prompt_1);
                                dialog.setTitle("Enter password");
                                final EditText editText = (EditText) dialog.findViewById(R.id.edtPrompt);
                                Button button = (Button) dialog.findViewById(R.id.buttonConfirm);
                                button.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        String password = editText.getText().toString();
                                        if ("5126".equals(password)) {
                                            showFragment(BaseFragment.DRAWING_FRAGMENT);
                                            mMainNavigator.closeDrawer(mSideMenu);
                                            dialog.cancel();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                                dialog.show();
                                break;
                            case "Translate":
                                showFragment(BaseFragment.TRANSLATE_FRAGMENT);
                                break;

                            case "Searching online":
                                showFragment(BaseFragment.WEB_FRAGMENT);
                                break;

                            default:
                                for (int i = 0; i < mItems.size(); i++) {
                                    mItems.get(i).mFocused = i == position;
                                }
                                mSideMenuAdapter.notifyDataSetChanged();
                                break;
                        }
                    }
                };
                asyncTask.execute();
            }
        });
        Bitmap bitmap = SupportUtils.getAvatar();
        if (bitmap != null) {
            mAvatar.setImageBitmap(bitmap);
        }
        mTvUsername.setText(SharedPreferenceUtils.getCurrentName());
        if (mFragmentManager.getBackStackEntryCount() > 0) {
            List<Fragment> list = mFragmentManager.getFragments();
            for (Fragment fragment : list) {
                BaseFragment baseFragment = (BaseFragment) fragment;
                if (baseFragment != null) {
                    baseFragment.setListener(this);
                    if (baseFragment.fragmentIdentify().equals(BaseFragment.MAIN_FRAGMENT)) {
                        mMainFragment = (MainFragment) baseFragment;
                        if (!LandScape) {
                            mMainFragment.setShowNoteListener(this);
                        } else {
                            ViewNoteFragment viewNoteFragment =
                                    (ViewNoteFragment) mFragmentManager.findFragmentById(R.id.layoutSubFragmentContainer);
                            mMainFragment.setShowNoteListener(viewNoteFragment);
                        }
                    }

                    if (baseFragment.fragmentIdentify().equals(BaseFragment.VIEW_NOTE_FRAGMENT)) {
                        ViewNoteFragment viewNoteFragment = (ViewNoteFragment) baseFragment;
                        viewNoteFragment.setNote(mCurrentNote);
                        if (mMainFragment != null) {
                            mMainFragment.setShowNoteListener(viewNoteFragment);
                        }
                    }
                }
            }

            if (mStack.isEmpty()) {
                if (!list.isEmpty()) {
                    for (int i = 0; i < list.size(); i++) {
                        BaseFragment baseFragment = (BaseFragment) list.get(i);
                        if (baseFragment != null) {
                            mStack.push(baseFragment.fragmentIdentify());
                        }
                    }
                }
            }
            checkScreen();
        }

        ScheduledExecutorService mService = Executors.newSingleThreadScheduledExecutor();
        mService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                final String available = mHandNote.checkInternetAvailability() ? "available" : "not available";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvInternet.setText("Internet: " + available);
                    }
                });
            }
        }, 10, 10, TimeUnit.SECONDS);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (!mSetTabSize) {
            mTabWidth = mBottomTabs.getWidth();
            mTabHeight = mBottomTabs.getHeight();
            mSetTabSize = true;
        }
        DisplayMetrics metrics = updateButtonAdd();
        if (mLayoutSubFragment != null) {
            LandScape = true;
        }
        if (mFragmentManager.getBackStackEntryCount() <= 0) {
            showFragment(mFragmentName);
        }
        float dip_5 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, metrics);
        int avatarHeight = mLayoutAvatar.getHeight();
        mAvatar.requestLayout();
        mAvatar.getLayoutParams().width = avatarHeight - 2 * dip_5 > 0 ? (int) (avatarHeight - 2 * dip_5) : avatarHeight;
        mAvatar.getLayoutParams().height = avatarHeight - 2 * dip_5 > 0 ? (int) (avatarHeight - 2 * dip_5) : avatarHeight;
        mLayoutLoading.setVisibility(View.GONE);
    }

    private DisplayMetrics updateButtonAdd() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float border = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, metrics);
        mButtonCreate.requestLayout();
        mButtonCreate.getLayoutParams().height = mTabHeight - 3 * border > 0 ? (int) (mTabHeight - 3 * border) : mTabHeight;
        mButtonCreate.getLayoutParams().width = mTabHeight - 3 * border > 0 ? (int) (mTabHeight - 3 * border) : mTabHeight;
        mBorderButtonCreate.requestLayout();
        mBorderButtonCreate.getLayoutParams().height = mTabHeight - 2 * border > 0 ? (int) (mTabHeight - 2 * border) : mTabHeight;
        mBorderButtonCreate.getLayoutParams().width = mTabHeight - 2 * border > 0 ? (int) (mTabHeight - 2 * border) : mTabHeight;
        return metrics;
    }

    private void initMapNames() {
        mGlobalMapNames = new ArrayList<>();
        try {
            String data = SupportUtils.getStringData("Trained", "MapNames.txt");
            if (data.length() == 0) {
                data = SupportUtils.getStringResource(this, R.raw.map_names);
                SupportUtils.writeFile(data, "Trained", "MapNames.txt");
            }
            StringTokenizer tokenizer = new StringTokenizer(data, "\r\n");
            ArrayList<String> listTokens = new ArrayList<>();
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (token != null) {
                    if (token.length() != 0) {
                        listTokens.add(token);
                    }
                }
            }

            if (!listTokens.isEmpty()) {
                for (int i = 0; i < listTokens.size(); i++) {
                    ClusterLabel clusterLabel = new ClusterLabel();
                    StringTokenizer tokenizer1 = new StringTokenizer(listTokens.get(i), ";");
                    ArrayList<String> strings = new ArrayList<>();
                    while (tokenizer1.hasMoreTokens()) {
                        String string = tokenizer1.nextToken();
                        if (!"".equals(string)) {
                            strings.add(string);
                        }
                    }

                    if (!strings.isEmpty()) {
                        for (String string : strings) {
                            String[] s = string.split(":");
                            if (s.length == 2) {
                                int count = Integer.valueOf(s[1]);
                                if (count > 0) {
                                    clusterLabel.addNewLabel(new Label(s[0], count));
                                }
                            }
                        }
                    }

                    mGlobalMapNames.add(clusterLabel);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int sum = 0;
        for (ClusterLabel clusterLabel : mGlobalMapNames) {
            sum += clusterLabel.getTotal();
            String mapNames = "";
            for (Label label : clusterLabel.getListLabel()) {
                mapNames += label.getLabel() + ":" + clusterLabel.getLabelPercentage(label) + "-" + label.getCount() + ";";
            }

            Log.d("List names", mapNames);
        }
        Log.d("Sum: ", "" + sum);
    }

    @Override
    public void initSOM() {
        try {
            String data = SupportUtils.getStringData("Trained", "SOM.txt");
            if (data.length() == 0) {
                data = SupportUtils.getStringResource(this, R.raw.som);
                SupportUtils.writeFile(data, "Trained", "SOM.txt");
            }
            StringTokenizer tokenizer = new StringTokenizer(data, "|");
            ArrayList<String> listTokens = new ArrayList<>();
            while (tokenizer.hasMoreTokens()) {
                String token = tokenizer.nextToken();
                if (token != null && !"".equals(token)) {
                    listTokens.add(token);
                }
            }

            if (!listTokens.isEmpty()) {
                double[][] weightMatrix = new double[SOM.NUMBERS_OF_CLUSTER][Input.VECTOR_DIMENSIONS];
                for (int i = 0; i < listTokens.size(); i++) {
                    String stringWeight = listTokens.get(i);
                    StringTokenizer stringTokenizer = new StringTokenizer(stringWeight, ";");
                    if (stringTokenizer.countTokens() == Input.VECTOR_DIMENSIONS) {
                        int index = 0;
                        while (stringTokenizer.hasMoreTokens()) {
                            weightMatrix[i][index] = Double.valueOf(stringTokenizer.nextToken());
                            index++;
                        }
                    }
                }

                mGlobalSOM = new SOM(weightMatrix);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void screenOrientation(String fragmentName) {
        Configuration configuration = getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (!configuration.isLayoutSizeAtLeast(Configuration.SCREENLAYOUT_SIZE_XLARGE)) {
                if (fragmentName.equals(BaseFragment.MAIN_FRAGMENT) || fragmentName.equals(BaseFragment.TEMPLATES_FRAGMENT)) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
                }
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } else {
            if (fragmentName.equals(BaseFragment.MAIN_FRAGMENT) || fragmentName.equals(BaseFragment.TEMPLATES_FRAGMENT)) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("Fragment", mFragmentName);
        outState.putBoolean("isSetSize", mSetTabSize);
        outState.putBoolean("LandScape", LandScape);
        outState.putInt("tabWidth", mTabWidth);
        outState.putInt("tabHeight", mTabHeight);
        ArrayList<String> list = new ArrayList<>();
        List<Fragment> listFragments = mFragmentManager.getFragments();
        for (Fragment fragment : listFragments) {
            BaseFragment baseFragment = (BaseFragment) fragment;
            if (baseFragment != null) {
                if (baseFragment.fragmentIdentify().equals(BaseFragment.VIEW_NOTE_FRAGMENT)) {
                    ViewNoteFragment viewNoteFragment = (ViewNoteFragment) baseFragment;
                    Note note = viewNoteFragment.getNote();
                    if (note != null) {
                        outState.putSerializable("Note", note);
                    }
                }
            }
        }
        while (!mStack.isEmpty()) {
            list.add(mStack.pop());
        }
        outState.putStringArrayList("Stack", list);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void showFragment(String name) {
        BaseFragment baseFragment = null;
        if (mStack != null) {
            if (!mStack.isEmpty()) {
                String current = mStack.peek();
                if (current.equals(name)) {
                    Toast.makeText(MainActivity.this, "You're in this screen now", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    if ((current.equals(BaseFragment.TEMPLATES_FRAGMENT) && name.equals(BaseFragment.MAIN_FRAGMENT))
                            || (name.equals(BaseFragment.TEMPLATES_FRAGMENT) && current.equals(BaseFragment.MAIN_FRAGMENT))) {
                        mStack.pop();
                    }
                }
            }
        }
        switch (name) {
            case BaseFragment.MAIN_FRAGMENT:
                MainFragment mainFragment = new MainFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("TabHeight", mTabHeight);
                mainFragment.setArguments(bundle);
                if (!LandScape) {
                    mainFragment.setShowNoteListener(this);
                } else {
                    ViewNoteFragment viewNoteFragment = new ViewNoteFragment();
                    mainFragment.setShowNoteListener(viewNoteFragment);
                    FragmentTransaction transaction = mFragmentManager.beginTransaction();
                    transaction.replace(R.id.layoutSubFragmentContainer, viewNoteFragment, viewNoteFragment.fragmentIdentify());
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
                baseFragment = mainFragment;
                mMainNavigator.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                break;

            case BaseFragment.DRAWING_FRAGMENT:
                baseFragment = new DrawingFragment();
                mBackPressListener = (DrawingFragment) baseFragment;
                mBackClickCount = 1;
                mMainNavigator.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                break;

            case BaseFragment.LEARNING_FRAGMENT:
                baseFragment = new LearningFragment();
                mBackClickCount = 1;
                mMainNavigator.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                break;

            case BaseFragment.CREATE_NOTE_FRAGMENT:
                baseFragment = new CreateNoteFragment();
                mBackPressListener = (CreateNoteFragment) baseFragment;
                mBackClickCount = 1;
                mMainNavigator.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                break;

            case BaseFragment.VIEW_NOTE_FRAGMENT:
                ViewNoteFragment viewNoteFragment = new ViewNoteFragment();
                mBackPressListener = viewNoteFragment;
                baseFragment = viewNoteFragment;
                mBackClickCount = 1;
                mMainNavigator.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                break;

            case BaseFragment.TEMPLATES_FRAGMENT:
                Bundle b = new Bundle();
                baseFragment = new TemplatesFragment();
                b.putInt("TabHeight", mTabHeight);
                baseFragment.setArguments(b);
                mMainNavigator.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                break;

            case BaseFragment.WEB_FRAGMENT:
                baseFragment = new WebFragment();
                mBackPressListener = (WebFragment) baseFragment;
                mBackClickCount = 1;
                mMainNavigator.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                break;

            case BaseFragment.TRANSLATE_FRAGMENT:
                baseFragment = new TranslateFragment();
                mBackPressListener = (TranslateFragment) baseFragment;
                mBackClickCount = 1;
                mMainNavigator.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                break;

            default:
                break;
        }

        if (baseFragment != null) {
            mFragmentName = name;
            screenOrientation(mFragmentName);
            baseFragment.setListener(this);
            mStack.push(name);
            addFragment(baseFragment, baseFragment.fragmentIdentify());
        }

        checkScreen();
    }

    private void checkScreen() {
        String name = "";
        if (!mStack.isEmpty()) {
            name = mStack.peek();
        } else {
            if (mFragmentName != null) {
                name = mFragmentName;
            }
        }
        switch (name) {
            case BaseFragment.DRAWING_FRAGMENT:
                for (SideMenuItem item : mItems) {
                    item.mFocused = item.mTitle.equals(getString(R.string.training_en));
                }
                this.toggleMainBottomTabs(false);
                if (mLayoutSubFragment != null) {
                    mLayoutSubFragment.setVisibility(View.GONE);
                }
                mMainNavigator.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                break;
            case BaseFragment.LEARNING_FRAGMENT:
                for (SideMenuItem item : mItems) {
                    item.mFocused = item.mTitle.equals(getString(R.string.training_en));
                }
                this.toggleMainBottomTabs(false);
                if (mLayoutSubFragment != null) {
                    mLayoutSubFragment.setVisibility(View.GONE);
                }
                mMainNavigator.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                break;
            case BaseFragment.TRANSLATE_FRAGMENT:
                for (SideMenuItem item : mItems) {
                    item.mFocused = item.mTitle.equals(getString(R.string.translate_en));
                }
                this.toggleMainBottomTabs(false);
                if (mLayoutSubFragment != null) {
                    mLayoutSubFragment.setVisibility(View.GONE);
                }
                mMainNavigator.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                break;
            case BaseFragment.CREATE_NOTE_FRAGMENT:
                this.toggleMainBottomTabs(false);
                if (mLayoutSubFragment != null) {
                    mLayoutSubFragment.setVisibility(View.GONE);
                }
                mMainNavigator.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                break;
            case BaseFragment.VIEW_NOTE_FRAGMENT:
                this.toggleMainBottomTabs(false);
                if (mLayoutSubFragment != null) {
                    mLayoutSubFragment.setVisibility(View.GONE);
                }
                mMainNavigator.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                break;
            case BaseFragment.TEMPLATES_FRAGMENT:
                for (SideMenuItem item : mItems) {
                    item.mFocused = item.mTitle.equals(getString(R.string.templates_fragment_en));
                }
                this.toggleMainBottomTabs(true);
                mTabNotes.setSelected(false);
                mTabTemplates.setSelected(true);
                if (mLayoutSubFragment != null) {
                    mLayoutSubFragment.setVisibility(View.GONE);
                }
                mMainNavigator.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                break;

            case BaseFragment.WEB_FRAGMENT:
                for (SideMenuItem item : mItems) {
                    item.mFocused = item.mTitle.equals(getString(R.string.web_en));
                }
                this.toggleMainBottomTabs(false);
                if (mLayoutSubFragment != null) {
                    mLayoutSubFragment.setVisibility(View.GONE);
                }
                mMainNavigator.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                break;
            default:
                for (SideMenuItem item : mItems) {
                    item.mFocused = false;
                }
                if (mLayoutSubFragment != null) {
                    mLayoutSubFragment.setVisibility(View.VISIBLE);
                }
                mMainNavigator.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                this.toggleMainBottomTabs(true);
                mTabNotes.setSelected(true);
                mTabTemplates.setSelected(false);
                break;
        }

        mSideMenuAdapter.notifyDataSetChanged();

        mTvAppTitle.setText(LanguageUtils.getFragmentTitle(name, this));

        if (mStack.size() > 1) {
            mButtonBack.setVisibility(View.VISIBLE);
            mButtonNavigator.setVisibility(View.GONE);
        } else {
            mButtonBack.setVisibility(View.GONE);
            mButtonNavigator.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void toggleMainNavigator(boolean show) {
        if (show) {
            mMainNavigator.openDrawer(mSideMenu);
            return;
        }
        mMainNavigator.closeDrawer(mSideMenu);
    }

    @Override
    public void toggleMainBottomTabs(boolean show) {
        mLayoutBottomTabs.setVisibility((show) ? View.VISIBLE : View.GONE);
    }

    @Override
    public SOM getGlobalSOM() {
        return mGlobalSOM;
    }

    @Override
    public ArrayList<ClusterLabel> getMapNames() {
        return mGlobalMapNames;
    }

    @Override
    public Note getCurrentNote() {
        return mCurrentNote;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.buttonMainNavigator:
                this.toggleMainNavigator(true);
                break;
            case R.id.buttonCreate:
                showFragment(BaseFragment.CREATE_NOTE_FRAGMENT);
                break;
            case R.id.buttonBack:
                onBackPressed();
                break;
            case R.id.buttonTemplates:
                showFragment(BaseFragment.TEMPLATES_FRAGMENT);
                break;
            case R.id.buttonListNotes:
                showFragment(BaseFragment.MAIN_FRAGMENT);
                break;
            case R.id.buttonMenu:
                mPopUpMenu.show();
                break;
            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mBackPressListener == null || !mBackPressListener.doBack()) {
            if (!mStack.isEmpty()) {
                mFragmentName = mStack.pop();
            }
            if (!doBack()) {
                super.onBackPressed();
                if (!mStack.isEmpty()) {
                    screenOrientation(mStack.peek());
                } else {
                    screenOrientation(mFragmentName);
                }
            } else {
                if (mBackClickCount > 0) {
                    mBackClickCount--;
                    mStack.push(mFragmentName);
                    Toast.makeText(MainActivity.this, "Press again to exit", Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                }
            }
            checkScreen();
            updateButtonAdd();
        }
    }

    @Override
    public boolean doBack() {
        return mFragmentName.equals(BaseFragment.MAIN_FRAGMENT) || mFragmentName.equals(BaseFragment.TEMPLATES_FRAGMENT);
    }

    private void addFragment(Fragment fragment, String name) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.layoutFragmentContainer, fragment, name);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onStop() {
        onSaveInstanceState(new Bundle());
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showNote(Note note) {
        mCurrentNote = note;
        showFragment(BaseFragment.VIEW_NOTE_FRAGMENT);
    }
}
