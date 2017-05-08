package com.inqbarna.adapters;

import android.databinding.ViewDataBinding;
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;

import java.util.Collection;

/**
 * @author Ricard Aparicio <ricard.aparicio@inqbarna.com>
 * @version 1.0 17/08/16
 */
public class BindingHolder extends RecyclerView.ViewHolder implements GroupIndicator {
    private ViewDataBinding mDataBinding;

    private BasicIndicatorDelegate mIndicatorHolderDelegate;

    BindingHolder(ViewDataBinding binding) {
        super(binding.getRoot());
        mDataBinding = binding;
        mIndicatorHolderDelegate = new BasicIndicatorDelegate();
    }

    @Override
    public int color() {
        return mIndicatorHolderDelegate.color();
    }

    @Override
    public boolean enabled() {
        return mIndicatorHolderDelegate.enabled();
    }

    @Override
    public void setColor(int color) {
        mIndicatorHolderDelegate.setColor(color);
    }

    @Override
    public void setEnabled(boolean enabled) {
        mIndicatorHolderDelegate.setEnabled(enabled);
    }

    public void bindValues(Collection<Pair<Integer, Object>> values) {
        if (null != values) {
            for (Pair<Integer, Object> val : values) {
                bindValue(val.first, val.second, false);
            }
            mDataBinding.executePendingBindings();
        }
    }

    private void bindValue(int varId, Object val, boolean execPending) {
        mDataBinding.setVariable(varId, val);
        if (execPending) {
            mDataBinding.executePendingBindings();
        }
    }

    public ViewDataBinding getDataBinding() {
        return mDataBinding;
    }

    SafeVariableBinding lockVars() {
        return new LockedVarsSet(this);
    }

    private static class LockedVarsSet implements SafeVariableBinding {
        private final BindingHolder mHolder;

        LockedVarsSet(BindingHolder holder) {
            mHolder = holder;
        }

        @Override
        public void bindValue(int variable, Object value) {
            mHolder.bindValue(variable, value, false);
        }

        @Override
        public BindingHolder unlockVars() {
            mHolder.mDataBinding.executePendingBindings();
            return mHolder;
        }
    }
}
