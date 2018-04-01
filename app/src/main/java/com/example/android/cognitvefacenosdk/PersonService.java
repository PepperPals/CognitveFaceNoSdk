package com.example.android.cognitvefacenosdk;

import android.app.Application;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.example.android.cognitvefacenosdk.faceapi.FaceApiError;
import com.example.android.cognitvefacenosdk.faceapi.FaceData;
import com.example.android.cognitvefacenosdk.faceapi.IdentifyRequest;
import com.example.android.cognitvefacenosdk.faceapi.IdentifyResult;
import com.example.android.cognitvefacenosdk.faceapi.ImageTooSmall;
import com.example.android.cognitvefacenosdk.faceapi.MicrosoftFaceApi;
import com.example.android.cognitvefacenosdk.faceapi.Person;
import com.example.android.cognitvefacenosdk.faceapi.PersonGroup;
import com.example.android.cognitvefacenosdk.faceapi.TrainingStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonService extends Application {
    private static final String TAG = PersonService.class.getSimpleName();

    // Valid format should be a string composed by numbers, English letters in lower case, '-', '_', and no longer than 64 characters.
    private static final String PERSON_GROUP_ID = "cognitive_face_android_person_group_id";

    private static final String TRAIN_STATUS_IN_PROGRESS = "running";

    private static final String PERSON_GROUP_NOT_TRAINED = "PersonGroupNotTrained";

    private static final String MS_API_KEY = "<YOUR API KEY HERE>";

    private boolean initialised = false;

    protected MicrosoftFaceApi faceService;

    private Map<String, Person> people = new HashMap<>();

    public boolean isInitialised() {
        return initialised;
    }

    public void initialise() {
        this.initialised = true;
        this.faceService = ServiceGenerator.createService(MicrosoftFaceApi.class);
    }

    public Person getPersonById(String personId) {
        return people.get(personId);
    }

    public void addPerson(Person person) {
        Log.d(TAG, "Add person: " + person);
        people.put(person.getPersonId(), person);
    }

    public String getPersonGroupId() {
        return PERSON_GROUP_ID;
    }

    public static String getFaceApiKey() {
        return MS_API_KEY;
    }

    public MicrosoftFaceApi getFaceService() {
        return faceService;
    }

    public void identify(String personGroupId, List<String> faceIds, IdentifyCallback callback) {
        Log.d(TAG, "identifyPerson in group " + personGroupId + " with face IDs " + faceIds);
        IdentifyRequest identifyRequest = new IdentifyRequest();
        identifyRequest.setPersonGroupId(personGroupId);
        identifyRequest.setFaceIds(faceIds);
        Call<List<IdentifyResult>> request = faceService.identifyFaces(getFaceApiKey(), identifyRequest);
        request.enqueue(new Callback<List<IdentifyResult>>() {
            @Override
            public void onResponse(@NonNull Call<List<IdentifyResult>> call, @NonNull Response<List<IdentifyResult>> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Request to identify people succeeded: " + response.body());
                    callback.result(response.body());
                } else {
                    Log.e(TAG, "Request to identify person failed with code: " + response.code());
                    FaceApiError error = ServiceGenerator.parseFaceApiError(response);
                    Log.e(TAG, "Face API error = " + error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<IdentifyResult>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to identify person", t);
            }
        });
    }


    /*
     * Convert result of identify request to map from faceId to most likely person
     */
    public Map<String, Person> faceIdToPerson(@NonNull List<IdentifyResult> identifications) {
        Log.d(TAG, "Processing " + identifications.size() + " identifications");
        Map<String, Person> people = new HashMap<>();
        for (IdentifyResult r : identifications) {
            Log.d(TAG, "Processing identification: " + r);
            if ((null != r.getCandidates()) && (r.getCandidates().size() > 0)) {
                // assume first candidate is most likely
                final String personId = r.getCandidates().get(0).getPersonId();
                final Person person = getPersonById(personId);
                if (null != person) {
                    people.put(r.getFaceId(), person);
                }
            }
        }
        return people;
    }

    /*
     * For now we only support one face per person
     */
    public void enrollPerson(String personGroupId, Person person, Bitmap face) {
        Log.i(TAG, "enrollPerson: " + person + " in group " + personGroupId);
        Call<Person> request = faceService.createPersonGroupPerson(getFaceApiKey(), personGroupId, person);
        request.enqueue(new Callback<Person>() {
            @Override
            public void onResponse(@NonNull Call<Person> call, @NonNull Response<Person> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Created person: " + response.body());
                    final Person createdPerson = response.body();
                    if (null == createdPerson) {
                        Log.e(TAG, "Null body in response to create person group person");
                    }

                    // Created person now need to add their face
                    Log.d(TAG, "Add face to person: " + response.body());
                    Pair<byte[], Float> imageBytesAndScale;
                    try {
                        imageBytesAndScale = Util.bitmapToSizeLimitedByteArray(face);
                    } catch (ImageTooSmall e) {
                        Log.e(TAG, "Face too small to add");
                        return;
                    }
                    RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), imageBytesAndScale.first);
                    assert createdPerson != null;
                    Call<FaceData> request = faceService.addFacePersonGroupPerson(getFaceApiKey(), personGroupId, createdPerson.getPersonId(), body);
                    request.enqueue(new Callback<FaceData>() {
                        @Override
                        public void onResponse(@NonNull Call<FaceData> call, @NonNull Response<FaceData> response) {
                            if (response.isSuccessful()) {
                                Log.i(TAG, "Person :" + person + " enrolled successfully with ID " + createdPerson.getPersonId() + " and face" + response.body());
                                person.setPersonId(createdPerson.getPersonId());
                                Log.d(TAG, "Adding person to cache: " + person);
                                addPerson(person);

                                trainPersonGroup(personGroupId);
                            } else {
                                Log.e(TAG, "Request add face to person failed with code: " + response.code());
                                FaceApiError error = ServiceGenerator.parseFaceApiError(response);
                                Log.e(TAG, "Face API error = " + error);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<FaceData> call, @NonNull Throwable t) {
                            Log.e(TAG, "Failed to add face to person: " + createdPerson);
                        }
                    });

                } else {
                    Log.e(TAG, "Request create person in group failed with code: " + response.code());
                    FaceApiError error = ServiceGenerator.parseFaceApiError(response);
                    Log.e(TAG, "Face API error = " + error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Person> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to create person " + person + " in group " + personGroupId);
            }
        });
    }

    public void trainPersonGroup(String personGroupId) {
        Log.i(TAG, "train person group: " + personGroupId);
        Call<TrainingStatus> request = faceService.getPersonGroupTrainingStatus(getFaceApiKey(), personGroupId);
        request.enqueue(new Callback<TrainingStatus>() {
            @Override
            public void onResponse(Call<TrainingStatus> call, Response<TrainingStatus> response) {
                if (response.isSuccessful()) {
                    final TrainingStatus status = response.body();
                    if (null != status) {
                        Log.i(TAG, "Training status for group: " + personGroupId + " is :" + status.getStatus());
                        if (!TRAIN_STATUS_IN_PROGRESS.equalsIgnoreCase(status.getStatus())) {
                            Log.d(TAG, "Trigger training for: " + personGroupId);
                            doTrainGroup(personGroupId);
                        }
                    } else {
                        Log.e(TAG, "Training status was null");
                    }
                } else {
                    Log.e(TAG, "Failed to get training status with code: " + response.code());
                    FaceApiError error = ServiceGenerator.parseFaceApiError(response);
                    Log.e(TAG, "Face API error = " + error);

                    if (groupHasNeverBeenTrained(response, error)) {
                        Log.i(TAG, "Group: " + personGroupId + " has never been trained before so no status is available");
                        doTrainGroup(personGroupId);
                    }
                }
            }

            @Override
            public void onFailure(Call<TrainingStatus> call, Throwable t) {
                Log.e(TAG, "Failed to get training status for:" + personGroupId);
            }
        });
    }

    private boolean groupHasNeverBeenTrained(Response<TrainingStatus> response, FaceApiError error) {
        String errorCode = null;
        if ((null != error) && (null != error.getError())) {
            errorCode = error.getError().getCode();
        }

        return (404 == response.code()) || PERSON_GROUP_NOT_TRAINED.equalsIgnoreCase(errorCode);
    }

    private void doTrainGroup(String personGroupId) {
        Call<Void> trainRequest = faceService.trainPersonGroup(getFaceApiKey(), personGroupId);
        trainRequest.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Successfully requested training for group: " + personGroupId);
                } else {
                    Log.e(TAG, "Failed to train with code: " + response.code());
                    FaceApiError error = ServiceGenerator.parseFaceApiError(response);
                    Log.e(TAG, "Face API error = " + error);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Failed to request trainging for group: " + personGroupId, t);
            }
        });
    }

    /**
     * Get the person group if it already exists, otherwise create it
     */
    public void initPersonGroup(PersonGroup group) {
        Log.i(TAG, "InitPersonGroup: " + group);
        getPersonGroup(group.getPersonGroupId(), (g) -> {
            if (null == g) {
                Log.i(TAG, "Group does not exist, need to create it");
                createPersonGroup(group, (k) -> {
                    if (null == k) {
                        Log.e(TAG, "Failed to create person group");
                    } else {
                        Log.i(TAG, "Created person group");
                    }
                });
            } else {
                Log.i(TAG, "Person group already exists, getting members");
                Call<List<Person>> request = faceService.listPersonGroupMembers(getFaceApiKey(), group.getPersonGroupId());
                request.enqueue(new Callback<List<Person>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Person>> call, @NonNull Response<List<Person>> response) {
                        if (response.isSuccessful()) {
                            Log.i(TAG, "Got members of person group");
                            if (response.body() != null) {
                                for (Person p : response.body()) {
                                    addPerson(p);
                                }
                            }
                        } else {
                            Log.d(TAG, "Request to list person group members failed with code: " + response.code());
                            FaceApiError error = ServiceGenerator.parseFaceApiError(response);
                            Log.e(TAG, "Face API error = " + error);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Person>> call, @NonNull Throwable t) {
                        Log.e(TAG, "Get Person group members failed.", t);
                    }
                });
            }
        });
    }

    private void getPersonGroup(String personGroupId, PersonGroupCallback callback) {
        Call<PersonGroup> request = faceService.getPersonGroup(getFaceApiKey(), personGroupId);
        Log.d(TAG, "Executing detect faces request");
        request.enqueue(new Callback<PersonGroup>() {
            @Override
            public void onResponse(@NonNull Call<PersonGroup> call, @NonNull Response<PersonGroup> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Get person group succeeded");
                    callback.result(response.body());
                } else {
                    Log.d(TAG, "Request failed with code: " + response.code());
                    FaceApiError error = ServiceGenerator.parseFaceApiError(response);
                    Log.e(TAG, "Face API error = " + error);
                    callback.result(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<PersonGroup> call, @NonNull Throwable t) {
                Log.e(TAG, "Get Person group failed.", t);
            }
        });
    }

    private void createPersonGroup(final PersonGroup group, PersonGroupCallback callback) {
        Call<Void> request = faceService.createPersonGroup(getFaceApiKey(), group.getPersonGroupId(), group);
        Log.d(TAG, "Executing detect faces request");
        request.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Create person group succeeded");
                    callback.result(group);
                } else {
                    Log.d(TAG, "Request failed with code: " + response.code());
                    FaceApiError error = ServiceGenerator.parseFaceApiError(response);
                    Log.e(TAG, "Face API error = " + error);
                    callback.result(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Create Person group failed.", t);
            }
        });
    }

    @FunctionalInterface
    public interface PersonGroupCallback {
        void result(PersonGroup personGroup);
    }

    @FunctionalInterface
    public interface IdentifyCallback {
        void result(List<IdentifyResult> faceResults);
    }
}
