package com.todotxt.todotxttouch.task;

import android.util.Log;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.example.allu.myapplication.backend.taskApi.TaskApi;
import com.example.allu.myapplication.backend.taskApi.model.TaskBean;
import com.todotxt.todotxttouch.TodoPreferences;
import com.todotxt.todotxttouch.util.TaskIo;

import java.io.IOException;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

public class EndpointsTaskBagImpl extends TaskBagImpl {
    final TaskApi taskApiService;

    // Constructor
    public EndpointsTaskBagImpl(TodoPreferences preferences, LocalTaskRepository localRepository) {
        super(preferences, localRepository, null);
        TaskApi.Builder builder = new TaskApi.Builder(AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(), null);
                /*.setRootUrl("http://192.168.1.102:8080/_ah/api/")
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                                                       @Override
                                                       public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest)
                                                               throws IOException {
                                                           abstractGoogleClientRequest.setDisableGZipContent(true);
                                                       }
                                                   }

                );*/
        taskApiService = builder.build();
    } // end of constructor, other methods to follow in this class...

    @Override
    public synchronized void pushToRemote(boolean overridePreference, boolean overwrite) {
        try {
            ArrayList<String> taskStrList =
                    TaskIo.loadTasksStrFromFile(LocalFileTaskRepository.TODO_TXT_FILE);
            taskApiService.clearTasks().execute();

            long id = 1;
            for (String taskStr : taskStrList) {
                TaskBean taskBean = new TaskBean();
                taskBean.setData(taskStr);
                taskBean.setId(id++);
                taskApiService.storeTask(taskBean).execute();
            }

            lastSync = new Date();

        } catch (IOException e) {
            Log.e(EndpointsTaskBagImpl.class.getSimpleName(),
                    "Error when storing tasks", e);
        }
    }

    @Override
    public synchronized void pullFromRemote(boolean overridePreference) {

        try {
            // Remote Call
            List<TaskBean> remoteTasks = taskApiService.getTasks().execute().getItems();

            if (remoteTasks != null) {
                ArrayList<Task> taskList = new ArrayList<Task>();
                for (TaskBean taskBean : remoteTasks) {
                    taskList.add(new Task(taskBean.getId(), taskBean.getData()));
                }
                store(taskList);
                reload();
                lastSync = new Date();
            }
        } catch (IOException e) {
            Log.e(EndpointsTaskBagImpl.class.getSimpleName(), "Error when loading tasks", e);
        }
    }

}