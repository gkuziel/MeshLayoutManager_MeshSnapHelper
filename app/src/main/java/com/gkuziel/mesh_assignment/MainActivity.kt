package com.gkuziel.mesh_assignment

import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.gkuziel.assignment.MeshLayoutManager
import com.gkuziel.assignment.MeshSnapHelper
import com.gkuziel.mesh_assignment.adapter.ItemAdapter
import com.gkuziel.mesh_assignment.databinding.ActivityMainBinding
import com.gkuziel.mesh_assignment.ui.LayoutManagerType
import com.gkuziel.mesh_assignment.ui.MainViewState
import com.gkuziel.mesh_assignment.ui.SnapHelperType


class MainActivity : AppCompatActivity() {

    private val itemAdapterTop by lazy { ItemAdapter() }
    private val itemAdapterBottom by lazy { ItemAdapter() }

    private var meshSnapHelper: MeshSnapHelper? = null
    private val linearSnapHelper by lazy { LinearSnapHelper() }
    private val pagerSnapHelper by lazy { PagerSnapHelper() }

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initLayout()
        viewModel.state.observe(this) {
            setState(it)
        }
        viewModel.itemsTop.observe(this) {
            itemAdapterTop.setItems(it)
        }
        viewModel.itemsBottom.observe(this) {
            itemAdapterBottom.setItems(it)
        }
    }

    private fun initLayout() {
        with(binding) {
            recyclerviewTop.adapter = itemAdapterTop
            recyclerviewBottom.adapter = itemAdapterBottom

            radioGroupLayoutManager.setOnCheckedChangeListener { group, checkedId ->
                viewModel.onLayoutManagerChecked(group.indexOfChild(findViewById(checkedId)))
            }
            radioGroupSnapHelper.setOnCheckedChangeListener { group, checkedId ->
                viewModel.onSnapHelperChecked(group.indexOfChild(findViewById(checkedId)))
            }
            switchReversed.setOnCheckedChangeListener { buttonView, isChecked ->
                viewModel.onReverseLayoutClicked(isChecked)
            }
            btnScrollToStart.setOnClickListener {
                recyclerviewTop.scrollTo(0)
            }
            btnScrollToEnd.setOnClickListener {
                recyclerviewTop.scrollTo(recyclerviewTop.adapter?.itemCount?.let {
                    it - 1
                } ?: 0)
            }
            btnScrollToPosition.setOnClickListener {
                val scrollPosition = etScrollPosition.text.toString().toIntOrNull() ?: 0
                recyclerviewTop.scrollTo(scrollPosition)
                viewModel.onScrollToChanged(scrollPosition)
            }

            btnAnimateItems.setOnClickListener {
                recyclerviewTop.startLayoutAnimation()
            }
            btnSetMeshDimension.setOnClickListener {
                viewModel.onDimensionChanged(
                    readColumnCount(),
                    readRowCount()
                )
                hideKeyboard()
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: View(this)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun readColumnCount() = binding.etMeshColumnNumbers.text.toString().toIntOrNull() ?: 5
    private fun readRowCount() = binding.etMeshRowNumbers.text.toString().toIntOrNull() ?: 2

    private fun setState(viewState: MainViewState) {
        with(binding) {
            etScrollPosition.setText(viewState.position.toString())
            switchReversed.isChecked = viewState.reversed
            (radioGroupLayoutManager.getChildAt(viewState.layoutManagerType.id) as? RadioButton)?.isChecked =
                true
            (radioGroupSnapHelper.getChildAt(viewState.snapHelperType.id) as? RadioButton)?.isChecked =
                true
            recyclerviewTop.setLayoutManager(viewState)
            recyclerviewBottom.layoutManager = LinearLayoutManager(
                this@MainActivity,
                RecyclerView.HORIZONTAL,
                false
            )
            setSnapHelpers(viewState)
        }
    }

    private fun RecyclerView.setLayoutManager(viewState: MainViewState) {
        layoutManager = when (viewState.layoutManagerType) {
            is LayoutManagerType.Mesh ->
                MeshLayoutManager(
                    this@MainActivity,
                    viewState.columnCount,
                    viewState.rowCount,
                    viewState.reversed
                )

            is LayoutManagerType.Linear ->
                LinearLayoutManager(
                    this@MainActivity,
                    RecyclerView.HORIZONTAL,
                    viewState.reversed
                )

            is LayoutManagerType.Grid ->
                GridLayoutManager(
                    this@MainActivity,
                    viewState.rowCount,
                    RecyclerView.HORIZONTAL,
                    viewState.reversed
                )
        }
    }

    private fun RecyclerView.scrollTo(position: Int) {
        if (binding.switchSmooth.isChecked) {
            smoothScrollToPosition(position)
        } else {
            scrollToPosition(position)
        }
    }

    private fun setSnapHelpers(viewState: MainViewState) {
        with(binding) {
            detachSnapHelpers()
            when (viewState.snapHelperType) {
                is SnapHelperType.Mesh -> {
                    meshSnapHelper = MeshSnapHelper(
                        readColumnCount(),
                        readRowCount(),
                        viewState.reversed
                    )
                    meshSnapHelper?.attachToRecyclerView(recyclerviewTop)
                }

                is SnapHelperType.Linear ->
                    linearSnapHelper.attachToRecyclerView(recyclerviewTop)

                is SnapHelperType.Pager ->
                    pagerSnapHelper.attachToRecyclerView(recyclerviewTop)

                is SnapHelperType.None -> {
                    // already detached
                }

            }
        }
    }

    private fun detachSnapHelpers() {
        linearSnapHelper.attachToRecyclerView(null)
        pagerSnapHelper.attachToRecyclerView(null)
        meshSnapHelper?.attachToRecyclerView(null)
    }
}