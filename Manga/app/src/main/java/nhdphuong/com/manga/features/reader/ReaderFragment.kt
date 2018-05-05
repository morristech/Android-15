package nhdphuong.com.manga.features.reader

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import nhdphuong.com.manga.R
import nhdphuong.com.manga.databinding.FragmentReaderBinding
import nhdphuong.com.manga.supports.AnimationHelper
import nhdphuong.com.manga.views.adapters.BookReaderAdapter

/*
 * Created by nhdphuong on 5/5/18.
 */
class ReaderFragment : Fragment(), ReaderContract.View {
    private lateinit var mPresenter: ReaderContract.Presenter
    private lateinit var mBinding: FragmentReaderBinding
    override fun setPresenter(presenter: ReaderContract.Presenter) {
        mPresenter = presenter
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater!!, R.layout.fragment_reader, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.ibBack.setOnClickListener {
            activity.onBackPressed()
        }

        mBinding.ibDownload.setOnClickListener {

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mPresenter.start()
    }

    override fun showBookTitle(bookTitle: String) {

        mBinding.mtvBookTitle.let { mtvBookTitle ->
            mtvBookTitle.text = bookTitle
            AnimationHelper.startTextRunning(mtvBookTitle)
        }
    }

    override fun showBookPages(pageList: List<String>) {
        mBinding.vpPages.adapter = BookReaderAdapter(context, pageList, View.OnClickListener {
            if (mBinding.clReaderBottom.visibility == View.VISIBLE) {
                AnimationHelper.startSlideOutTop(activity, mBinding.clReaderTop, {
                    mBinding.clReaderTop.visibility = View.GONE
                })
                AnimationHelper.startSlideOutBottom(activity, mBinding.clReaderBottom, {
                    mBinding.clReaderBottom.visibility = View.GONE
                })
            } else {
                AnimationHelper.startSlideInTop(activity, mBinding.clReaderTop, {
                    mBinding.clReaderTop.visibility = View.VISIBLE
                })
                AnimationHelper.startSlideInBottom(activity, mBinding.clReaderBottom, {
                    mBinding.clReaderBottom.visibility = View.VISIBLE
                })
            }
        })
    }

    override fun jumpToPage(pageNumber: Int) {
        mBinding.vpPages.setCurrentItem(pageNumber, true)
    }

    override fun onStop() {
        super.onStop()
        mPresenter.stop()
    }

    override fun showLoading() {

    }

    override fun hideLoading() {

    }

}