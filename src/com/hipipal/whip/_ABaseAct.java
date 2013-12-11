package com.hipipal.whip;


import com.zuowuxuxi.util.NAction;
import com.zuowuxuxi.util.NUtil;
import com.zuowuxuxi.common.GDBase;

import greendroid.graphics.drawable.ActionBarDrawable;
import greendroid.widget.AsyncImageView;
import greendroid.widget.NormalActionBarItem;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class _ABaseAct extends GDBase  {
	protected static final String TAG = "_ABaseAct";
	
	/*
	protected ItemAdapter adapter;
	protected GestureDetector mGestureDetector;  
    protected static final int FLING_MIN_DISTANCE = 50;  
    protected static final int FLING_MIN_VELOCITY = 0; 
	*/

    
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    

	/*@Override
    public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    	switch (item.getItemId()) {
    		default:
    			mBar.show(item.getItemView());

    	}
    	return 	super.onHandleActionBarItemClick(item, position);
    }*/
	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    

    public void onBack(View v) {
    	finish();
    	//overridePendingTransition(R.anim.push_up_in, R.anim.push_up_out);
    }
  	
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public String confGetUpdateURL(int flag) {
		return CONF.UPDATE_URL;
	}

	@Override
	public Class<?> getUpdateSrv() {
		// TODO Auto-generated method stub
		return null;
	}
}
