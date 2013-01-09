package com.mwr.droidhg.agent;

import com.mwr.droidhg.Agent;
import com.mwr.droidhg.agent.views.EndpointListView;
import com.mwr.droidhg.agent.views.ServerListRowView;
import com.mwr.droidhg.api.Endpoint;
import com.mwr.droidhg.api.ServerParameters;

import android.os.Bundle;
import android.os.RemoteException;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	private EndpointListView endpoint_list_view = null;
	private ServerListRowView server_list_row_view = null;
	
	private void launchEndpointActivity(Endpoint endpoint) {
		Intent intent = new Intent(MainActivity.this, EndpointActivity.class);
		intent.putExtra("endpoint_id", endpoint.getId());
		
		MainActivity.this.startActivity(intent);
	}
	
	private void launchServerActivity() {
		MainActivity.this.startActivity(new Intent(MainActivity.this, ServerActivity.class));
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_main);
        
        Agent.setContext(this.getApplicationContext());
        
        this.endpoint_list_view = (EndpointListView)this.findViewById(R.id.endpoint_list_view);
        this.endpoint_list_view.setAdapter(new EndpointAdapter(this.getApplicationContext(), Agent.getEndpointManager()));
        this.endpoint_list_view.setOnEndpointSelectListener(new EndpointListView.OnEndpointSelectListener() {
			
			@Override
			public void onEndpointSelect(Endpoint endpoint) {
				MainActivity.this.launchEndpointActivity(endpoint);
			}
			
		});
        
        this.server_list_row_view = (ServerListRowView)this.findViewById(R.id.server_list_row_view);
        this.server_list_row_view.setServerParameters(Agent.getServerParameters());
        this.server_list_row_view.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				MainActivity.this.launchServerActivity();
			}
        	
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.menu_refresh:
    		this.updateEndpointStatuses();
    		this.updateServerStatus();
    		return true;
    	
    	case R.id.menu_settings:
    		this.startActivity(new Intent(this, SettingsActivity.class));
    		return true;
    	
    	default:
    		return super.onOptionsItemSelected(item);
    	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	
    	Agent.unbindServices();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	Agent.bindServices();
    }
    
    protected void updateEndpointStatuses() {
    	try {
			for(Endpoint e : Agent.getEndpointManager().all())
				e.setStatus(Endpoint.Status.UPDATING);
			
			Agent.getClientService().getEndpointStatuses(Agent.getMessenger());
		}
		catch(RemoteException e) {
			for(Endpoint e2 : Agent.getEndpointManager().all())
				e2.setStatus(Endpoint.Status.UNKNOWN);
			
			Toast.makeText(this, "problem, service not running", Toast.LENGTH_SHORT).show();
		}
    }
    
    protected void updateServerStatus() {
		try {
			Agent.getServerParameters().setStatus(ServerParameters.Status.UPDATING);
			
			Agent.getServerService().getServerStatus(Agent.getMessenger());
		}
		catch (RemoteException e) {
			Agent.getServerParameters().setStatus(Endpoint.Status.UNKNOWN);
			
			Toast.makeText(this, "problem, service not running", Toast.LENGTH_SHORT).show();
		}
    }
    
}
