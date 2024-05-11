package com.example.rxjava;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.vk.api.sdk.VK;
import com.vk.api.sdk.auth.VKScope;
import com.vk.api.sdk.exceptions.VKApiException;
import com.vk.api.sdk.requests.VKRequest;
import com.vk.api.sdk.utils.VKUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] fingerPrint = VKUtils.getCertificateFingerprint(this, this.getPackageName());
        Log.d("MyLog", fingerPrint[0]);

        if(!VK.isLoggedIn())
            authLauncher.launch(new ArrayList<>(Arrays.asList(VKScope.WALL, VKScope.FRIENDS, VKScope.GROUPS)));


        TextView textView = findViewById(R.id.tv);
        //String user_id = null;
        Observable obs = observableIdOwner("ig0rello")
                .map((Function<String, Integer>) s -> Integer.parseInt(s))
                .share(); //При отправке нескольких запросов формируется один
        obs.subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull Integer integer) {
                textView.setText(String.valueOf(integer));
                //user_id = String.valueOf(integer);
            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });

        TextView textView2 = findViewById(R.id.tv2);
        Observable obs2 = observableGetTenFriends("447359808")
                .share();
        obs2.subscribe(new Observer<ArrayList<Integer>>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onNext(@NonNull ArrayList<Integer> integers) {
                for (Integer integer: integers) {
                    textView2.setText(textView2.getText() + " " + integer);
                }

            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });


        User user = new User();
        Single single = user.getName()
                .map((Function<String, String>) s -> {
                    return s.concat("Lol");
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        single.subscribe(new SingleObserver<String>() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onSuccess(@NonNull String s) {

            }

            @Override
            public void onError(@NonNull Throwable e) {

            }

        });


        Completable completable = user.setName("Aboba")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(@NonNull Disposable d) {

            }

            @Override
            public void onComplete() {

            }

            @Override
            public void onError(@NonNull Throwable e) {

            }
        });

    }

    ActivityResultLauncher<Collection<VKScope>> authLauncher = VK.login(this, o -> {

    });

    public String getIdOwner(String screenName) {
        String id = "0";
        VKRequest<JSONObject> request = new VKRequest("users.get", VK.getApiVersion())
                .addParam("user_ids", screenName);
        try {
            try {
                JSONObject response = VK.executeSync(request);
                id = VK.executeSync(request)
                        .getJSONArray("response")
                        .getJSONObject(0)
                        .getString("id"); //В главном потоке
                Log.d("MyLog", String.valueOf(response));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (VKApiException e) {
            throw new RuntimeException(e);
        }
        //VK.execute(); //В другом потоке

        return id;
    }

    public ArrayList<Integer> getTenFriends(String screenName) {
        ArrayList<Integer> friendsIds = new ArrayList<>();
        VKRequest<JSONObject> request = new VKRequest("friends.get", VK.getApiVersion())
                .addParam("user_id", screenName);
        try {
            try {
                for(int i=0; i<10; i++) {
                    String response = VK.executeSync(request).getJSONObject("response")
                            .getJSONArray("items")
                            .getString(i);
                    friendsIds.add(Integer.parseInt(response));
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (VKApiException e) {
            throw new RuntimeException(e);
        }
        Log.d("MyLog", "FriendsIds: " + friendsIds);
        return friendsIds;
    }

    public Observable observableIdOwner(String screenName) {
        return Observable.fromCallable(() -> {
            return getIdOwner(screenName);
        }).onErrorResumeNext(throwable -> {
            return Observable.just("0");
        }).subscribeOn(Schedulers.io()) //выполнение операций в фоновом режиме
        .observeOn(AndroidSchedulers.mainThread()); //наблюдение (получение результата) в главном потоке
    }

    public Observable observableGetTenFriends(String screenName) {
        return Observable.fromCallable(() -> {
            return getTenFriends(screenName);
        }).onErrorResumeNext(throwable -> {
            return Observable.just(null);
        }).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread());
    }
}