package katsapov.room;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import katsapov.room.DAO.UserDataSource;
import katsapov.room.DAO.UserDatabase;
import katsapov.room.Database.UserRepository;
import katsapov.room.Model.User;

public class MainActivity extends AppCompatActivity {

    //Adapter
    List<User> userList = new ArrayList<>();
    ArrayAdapter<User> adapter;

    //Database
    private CompositeDisposable compositeDisposable;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        compositeDisposable = new CompositeDisposable();
        ListView listOfUsers = findViewById(R.id.listOfUsers);
        FloatingActionButton fab = findViewById(R.id.fab);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userList);
        registerForContextMenu(listOfUsers);
        listOfUsers.setAdapter(adapter);

        //Databese
        UserDatabase userDatabase = UserDatabase.getInstance(this);
        userRepository = UserRepository.getInstance(UserDataSource.getInstance(userDatabase.userDAO()));

        //Load all database
        loadData();

        //Event
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Add new user
                //random email
                Disposable disposable = Observable.create(new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> e) {
                        User user = new User("Sellwin",
                                UUID.randomUUID().toString() + "@");
                        userRepository.insertUser(user);
                        e.onComplete();
                    }
                })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer() {
                            @Override
                            public void accept(Object o) {
                                Toast.makeText(MainActivity.this, "User added", Toast.LENGTH_SHORT).show();
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                Toast.makeText(MainActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        },new Action() {
                            @Override
                            public void run() {
                                loadData();//refresh data
                            }
                        });
            }
        });
    }

    private void loadData() {
                //use RXJAVA
                Disposable disposable = userRepository.getAllUsers()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribe(new Consumer<List<User>>() {
                            @Override
                            public void accept(List<User> users) {
                                onGetAllUserSuccess(users);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                Toast.makeText(MainActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                compositeDisposable.add(disposable);
            }

    private void onGetAllUserSuccess(List<User> users) {
                userList.clear();
                userList.addAll(users);
                adapter.notifyDataSetChanged();
            }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_clear:
            deleteAllUsers();
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllUsers() {
        Disposable disposable = Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) {
                userRepository.deleteAllUsers();
                e.onComplete();
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer() {
                    @Override
                    public void accept(Object o) {
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Toast.makeText(MainActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },new Action() {
                    @Override
                    public void run() {
                        loadData();//refresh data
                    }
                });
        compositeDisposable.add(disposable);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        menu.setHeaderTitle("Select action:");
        menu.add(Menu.NONE,0,Menu.NONE,"UPDATE");
        menu.add(Menu.NONE,1,Menu.NONE,"DELETE");

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        final User user = userList.get(info.position);
        switch (item.getItemId()) {
            case 0: {
                final EditText edtName = new EditText(MainActivity.this);
                edtName.setText(user.getName());
                edtName.setHint("Enter your name");
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Edit")
                        .setMessage("Edit user name")
                        .setView(edtName)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (TextUtils.isEmpty(edtName.getText().toString()))
                                    return;
                                else {
                                    user.setName(edtName.getText().toString());
                                    updateUser(user);
                                }
                            }
                        }).setNegativeButton("Cansel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }
            break;

            case 1: //delete
            {
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Do you want Delete" + user.toString())
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteUser(user);
                            }
                        }).setNegativeButton("Cansel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();

            }
            break;
        }
        return true;
    }

    private void deleteUser(final User user) {
        Disposable disposable = Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) {
                userRepository.deleteUser(user);
                e.onComplete();
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer() {
                               @Override
                               public void accept(Object o) {
                               }
                           }, new Consumer<Throwable>() {
                               @Override
                               public void accept(Throwable throwable) {
                                   Toast.makeText(MainActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                               }
                           },new Action() {
                               @Override
                               public void run() {
                                   loadData();//refresh data
                               }

                           }
                );
        compositeDisposable.add(disposable);
    }
    private  void updateUser(final User user){
        Disposable disposable = Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> e) {
                userRepository.updateUser(user);
                e.onComplete();
            }
        })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer() {
                    @Override
                    public void accept(Object o) {
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Toast.makeText(MainActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },new Action() {
                    @Override
                    public void run() {
                        loadData();//refresh data
                    }

                }
                );
        compositeDisposable.add(disposable);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
    }
}

