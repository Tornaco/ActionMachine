package dev.nick.app.automachine;

import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import dev.nick.app.automachine.service.Command;
import dev.nick.app.automachine.tiles.PlanSetting;
import dev.nick.logger.Logger;
import dev.nick.logger.LoggerManager;

public class PlanBuilder extends TransactionSafeActivity implements PlanSetting.Callback {

    private RecyclerView mRecyclerView;
    private Adapter mAdapter;

    private Command.Commander mCommander;

    private Logger mLogger;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLogger = LoggerManager.getLogger(getClass());

        setContentView(R.layout.activity_planner);

        setTitle("UnDefined plan");

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        placeFragment(R.id.container, new PlanSetting(), null, true);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildCommands();
                try {
                    mCommander.buildXMLDoc(Machine.PLAN_DIR);
                    finish();
                } catch (IOException e) {
                    mLogger.error(e);
                } catch (JDOMException e) {
                    mLogger.error(e);
                }
            }
        });

        showCommandList();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_plan_builder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.cmd_home:
                addCommandToUI(new Command.Home());
                return true;
            case R.id.cmd_back:
                addCommandToUI(new Command.Back());
                return true;
            case R.id.cmd_sleep:
                showSleeping();
                return true;
            case R.id.cmd_tap:
                showTaping();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void addCommandToUI(Command command) {
        mLogger.debug("Adding:" + command);
        CommandWrapper wrapper = new CommandWrapper();
        wrapper.setC(command);
        wrapper.setPosition(mAdapter.getItemCount());
        mAdapter.add(wrapper, mAdapter.getItemCount());
    }

    @Override
    public void onNaming(String name) {
        mLogger.debug("Name:" + name);
        mCommander.name(name);
        setTitle(name);
    }

    private void buildCommands() {
        for (CommandWrapper commandWrapper : mAdapter.data) {
            Command c = commandWrapper.c;
            mCommander.command(c);
        }
    }

    private void showTaping() {
        final View editTextContainer = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text2, null, false);
        AlertDialog alertDialog = new AlertDialog.Builder(PlanBuilder.this)
                .setTitle("Taping at")
                .setView(editTextContainer)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        EditText x = (EditText) editTextContainer.findViewById(R.id.edit_text_x);
                        String xStr = x.getText().toString();
                        EditText y = (EditText) editTextContainer.findViewById(R.id.edit_text_y);
                        String yStr = y.getText().toString();
                        addCommandToUI(new Command.Tap(Integer.parseInt(xStr), Integer.parseInt(yStr)));
                    }
                })
                .show();
    }

    private void showSleeping() {
        final View editTextContainer = LayoutInflater.from(this).inflate(R.layout.dialog_edit_text, null, false);
        final EditText x = (EditText) editTextContainer.findViewById(R.id.edit_text);
        x.setInputType(InputType.TYPE_CLASS_NUMBER);
        x.setHint("1000");
        AlertDialog alertDialog = new AlertDialog.Builder(PlanBuilder.this)
                .setTitle("Sleeping within")
                .setView(editTextContainer)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String xStr = x.getText().toString();
                        mLogger.debug(xStr);
                        addCommandToUI(new Command.Sleep(Integer.parseInt(xStr)));
                    }
                })
                .show();
    }

    @Override
    public void onCnting(int c) {
        mCommander.count(c);
        mLogger.debug("Set count:" + c);
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

        private final List<CommandWrapper> data;

        Adapter(List<CommandWrapper> data) {
            this.data = data;
        }

        Adapter() {
            this(new ArrayList<CommandWrapper>());
        }

        void update(List<CommandWrapper> data) {
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

        public void add(CommandWrapper command, int position) {
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
            final Command item = data.get(position).getC();
            holder.title.setVisibility(View.VISIBLE);
            holder.title.setText(item.getName());
            holder.actionBtn.setText(item.getDescription());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.thumbnail.getLayoutParams();
                params.removeRule(RelativeLayout.CENTER_HORIZONTAL);
                holder.thumbnail.setLayoutParams(params);
            }
            holder.thumbnail.setImageResource(item.getIconRes());
            holder.actionBtn.setVisibility(position == 0 ? View.VISIBLE : View.INVISIBLE);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(PlanBuilder.this, holder.actionBtn);
                    popupMenu.inflate(R.menu.command_item_actions);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.action_remove:
                                    remove(holder.getAdapterPosition());
                                    onRemove(item);
                                    break;
                            }
                            return true;
                        }
                    });
                    popupMenu.show();
                }
            });

        }

        private void onRemove(Command command) {
            mLogger.debug("Removed:" + command);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }
    }
}
