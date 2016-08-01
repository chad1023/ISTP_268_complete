package com.hci.lab430.myapplication.adapter;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hci.lab430.myapplication.R;
import com.hci.lab430.myapplication.model.FilePath;
import com.hci.lab430.myapplication.model.PokemonInfo;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by lab430 on 16/7/16.
 */
public class PokemonInfoListViewAdapter extends ArrayAdapter<PokemonInfo> {

    int mRow_layout_id;
    LayoutInflater mInflater;
    Picasso mPicasso;
    public ArrayList<PokemonInfo> selectedPokemons;
    Activity mActivity;

    public PokemonInfoListViewAdapter(Activity activity,
                                      int resource,
                                      ArrayList<PokemonInfo> objects) {
        super(activity, resource, objects);
        mActivity = activity;
        mRow_layout_id = resource;
        mInflater = LayoutInflater.from(activity);
        mPicasso = Picasso.with(activity);
        selectedPokemons = new ArrayList<>();

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PokemonInfo dataItem = getItem(position);
        ViewHolder viewHolder = null;
        if(convertView == null) { //create a new one if it hasn't been initiated yet.
            convertView = mInflater.inflate(mRow_layout_id, parent, false);
            viewHolder = new ViewHolder(convertView, mPicasso, this);
            convertView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.setView(dataItem);

        return convertView;
    }

    void onPokemonSelectedChange(PokemonInfo pokemonInfo) {
        if(selectedPokemons.size() == 0) {
            mActivity.invalidateOptionsMenu();
        }

        if(pokemonInfo.isSelected) {
            selectedPokemons.add(pokemonInfo);
        }
        else {
            selectedPokemons.remove(pokemonInfo);
        }

        if(selectedPokemons.size() == 0) {
            mActivity.invalidateOptionsMenu();
        }
    }

    public PokemonInfo getItemWithName(String name) {

        for(int i = 0;i < getCount();i++) {
            PokemonInfo pokemonInfo = getItem(i);
            if(name.equals(pokemonInfo.name)) {
                return pokemonInfo;
            }
        }

        return null;
    }

    public void update(PokemonInfo newData) {
        PokemonInfo oldData = getItemWithName(newData.name);
        oldData.skill = newData.skill;
        oldData.currentHP = newData.currentHP;
        oldData.maxHP = newData.maxHP;
        oldData.level = newData.level;
        notifyDataSetChanged();
    }

    public void selectRow(View rowView) {
        //once a row view has been initiated, it should be bound with a view holder
        ViewHolder viewHolder = (ViewHolder)rowView.getTag();
        viewHolder.setSelected();
    }

    public static class ViewHolder implements View.OnClickListener{

        private View mRowView = null;
        private ImageView mAppearanceImg = null;
        private TextView mNameTxt = null;
        private TextView mLevelTxt = null;
        private TextView mCurrentHPTxt = null;
        private TextView mMaxHPTxt = null;
        private ProgressBar mHPBar = null;

        private Picasso mPicasso;
        private PokemonInfo mPokemonInfo;
        private PokemonInfoListViewAdapter mAdapter;

        public ViewHolder(View row_view, Picasso picasso, PokemonInfoListViewAdapter adapter) {
            mRowView = row_view;
            mAppearanceImg = (ImageView)row_view.findViewById(R.id.appearance_image);
            mNameTxt = (TextView) row_view.findViewById(R.id.name);
            mLevelTxt = (TextView) row_view.findViewById(R.id.level);
            mCurrentHPTxt = (TextView) row_view.findViewById(R.id.currentHP);
            mMaxHPTxt = (TextView) row_view.findViewById(R.id.maxHP);
            mHPBar = (ProgressBar) row_view.findViewById(R.id.HP_progressBar);
            mPicasso = picasso;
            mAdapter = adapter;
        }

        public void setView(PokemonInfo data) {
            mPokemonInfo = data;

            mRowView.setActivated(mPokemonInfo.isSelected);
            mPicasso.load(data.listImgId).into(mAppearanceImg);
            mAppearanceImg.setOnClickListener(this);

            mNameTxt.setText(data.name);
            mLevelTxt.setText(String.valueOf(data.level));
            mMaxHPTxt.setText(String.valueOf(data.maxHP));

            if(mPokemonInfo.isHealing && mPokemonInfo.currentHP < mPokemonInfo.maxHP) {
                animateHPBarAndCurrentHP();
            }
            else {
                mCurrentHPTxt.setText(String.valueOf(data.currentHP));
                mHPBar.setProgress((int)(((float)data.currentHP/data.maxHP) * 100));
            }

        }

        public void setSelected() {
            mPokemonInfo.isSelected = !mPokemonInfo.isSelected;
            mRowView.setActivated(mPokemonInfo.isSelected);
            mAdapter.onPokemonSelectedChange(mPokemonInfo);
        }

        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            if (viewId == R.id.appearance_image) {
                setSelected();
            }
        }

        private void animateHPBarAndCurrentHP() {
            //play animation
            int animationDuration = 1500;
            ObjectAnimator hpBarAnimator = ObjectAnimator.ofInt(mHPBar, "progress", mHPBar.getProgress(), 100);
            hpBarAnimator.setDuration(animationDuration);

            ValueAnimator hpTextAnimator = new ValueAnimator();
            hpTextAnimator.setObjectValues(mPokemonInfo.currentHP, mPokemonInfo.maxHP);// here you set the range, from 0 to "count" value
            hpTextAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    mPokemonInfo.currentHP = (int) animation.getAnimatedValue();
                    mCurrentHPTxt.setText(String.valueOf(mPokemonInfo.currentHP));
                }
            });
            hpTextAnimator.setDuration(animationDuration); // here you set the duration of the anim
            hpTextAnimator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {

                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    mPokemonInfo.isHealing = false;
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    mPokemonInfo.isHealing = false;
                }

                @Override
                public void onAnimationRepeat(Animator animator) {

                }
            });
            hpTextAnimator.start();
            hpBarAnimator.start();
        }

    }
}