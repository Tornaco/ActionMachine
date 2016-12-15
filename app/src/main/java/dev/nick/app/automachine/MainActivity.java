package dev.nick.app.automachine;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dev.nick.app.automachine.service.Command;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private Adapter mAdapter;

    private Command.Commander mCommander;

    private Logger mLogger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLogger = LoggerManager.getLogger(getClass());

        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), PlanBuilder.class));
            }
        });

        showCommandList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCommandList();
    }

    protected void showCommandList() {
        mCommander = new Command.Commander(this);
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new Adapter();
        mRecyclerView.setLayoutManager(
                new LinearLayoutManager(getApplicationContext(),
                        LinearLayoutManager.VERTICAL,
                        false));
        mRecyclerView.setAdapter(mAdapter);
    }

    private void loadCommandList() {
        AsyncTask.THREAD_POOL_EXECUTOR.execute(new Runnable() {
            @Override
            public void run() {
                File dir = new File(Machine.PLAN_DIR);
                File[] subFiles = dir.listFiles();
                if (subFiles == null || subFiles.length == 0) return;
                final List<CommandInfo> infos = new ArrayList<>(subFiles.length);
                for (File f : subFiles) {
                    if (f.isDirectory()) return;
                    CommandInfo info = new CommandInfo();
                    info.path = f.getAbsolutePath();
                    info.name = f.getName();
                    infos.add(info);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.update(infos);
                    }
                });
            }
        });
    }

    static class CommandInfo {
        String name;
        String path;
    }

    static class TwoLinesViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        TextView description;
        ImageView thumbnail;
        TextView actionBtn;

        TwoLinesViewHolder(final View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(android.R.id.title);
            description = (TextView) itemView.findViewById(android.R.id.text1);
            actionBtn = (TextView) itemView.findViewById(R.id.hint);
            thumbnail = (ImageView) itemView.findViewById(R.id.avatar);
        }
    }

    private class CommandWrapper {

        Command c;
        int position;

        public Command getC() {
            return c;
        }

        public void setC(Command c) {
            this.c = c;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }

    private class Adapter extends RecyclerView.Adapter<TwoLinesViewHolder> {

        private final List<CommandInfo> data;

        Adapter(List<CommandInfo> data) {
            this.data = data;
        }

        Adapter() {
            this(new ArrayList<CommandInfo>());
        }

        void update(List<CommandInfo> data) {
            this.data.clear();
            this.data.addAll(data);
            notifyDataSetChanged();
        }

        void remove(int position) {
            this.data.remove(position);
            notifyItemRemoved(position);
        }

        void clear() {
            this.data.clear();
            notifyDataSetChanged();
        }

        public void add(CommandInfo command, int position) {
            this.data.add(position, command);
            notifyDataSetChanged();
        }

        @Override
        public TwoLinesViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
            final View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.simple_card_item, parent, false);
            return new TwoLinesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final TwoLinesViewHolder holder, int position) {
            final CommandInfo item = data.get(position);
            holder.title.setVisibility(View.VISIBLE);
            holder.title.setText(item.name);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.thumbnail.getLayoutParams();
                params.removeRule(RelativeLayout.CENTER_HORIZONTAL);
                holder.thumbnail.setLayoutParams(params);
            }
            holder.thumbnail.setImageResource(R.drawable.ic_adb_black_24dp);
            holder.actionBtn.setVisibility(position == 0 ? View.VISIBLE : View.INVISIBLE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(MainActivity.this, holder.actionBtn);
                    popupMenu.inflate(R.menu.command_item_actions);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.action_remove:
                                    remove(holder.getAdapterPosition());
                                    onRemove(item);
                                    break;
                                case R.id.action_execute:
                                    Command.Commander.fromXml(MainActivity.this, item.path)
                                            .execute();
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });

        }

        private void onRemove(CommandInfo command) {
            mLogger.debug("Removed:" + command);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}
