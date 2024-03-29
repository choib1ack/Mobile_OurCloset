package com.example.mp_termproject.ourcloset;

import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.mp_termproject.R;
import com.example.mp_termproject.mycloset.add.MyClosetAddActivity;
import com.example.mp_termproject.mycloset.dto.ImageDTO;
import com.example.mp_termproject.ourcloset.dto.InfoDTO;
import com.example.mp_termproject.ourcloset.dto.RequestDTO;
import com.example.mp_termproject.ourcloset.filter.OurClosetFilterActivity;
import com.example.mp_termproject.ourcloset.gps.ShowMapWithDistanceActivity;
import com.example.mp_termproject.ourcloset.message.RequestMessageActivity;
import com.example.mp_termproject.ourcloset.message.ResponseMessageActivity;
import com.example.mp_termproject.ourcloset.request.RequestActivity;
import com.example.mp_termproject.ourcloset.viewinfo.ViewClosetInfoActivity;
import com.example.mp_termproject.signup.UserInfo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;


public class OurClosetFragment extends Fragment {

    private static final String TAG = "OurClosetFragment";

    static final int REQUEST_FILTER = 1;
    static final int NORMAL = 1;
    static final int SEARCH = 2;
    static final int FILTER = 3;
    static final int SORTING = 4;

    static int check = NORMAL;
    static int totalNum = 0;
    static int checkNum = 0;

    EditText searchText;
    ImageView searchImage;
    LinearLayout imageContainer;
    LinearLayout first;

    FirebaseUser user;
    FirebaseFirestore db;
    DocumentReference docRefUserInfo;
    FirebaseStorage storage;
    StorageReference storageRef;

    ArrayList<StorageReference> imageList;
    HashSet<InfoDTO> filterList;
    ArrayList<UserInfo> userInfoList;
    ArrayList<InfoDTO> infoDTOList;

    UserInfo myDTO;
    Double[] imgnum;

    Double[] myLoc;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Our Closet");
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_our_closet,
                container,
                false);
        setHasOptionsMenu(true);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();
        docRefUserInfo = db.collection("users").document(user.getUid());
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        imageList = new ArrayList<>();
        filterList = new HashSet<>();
        userInfoList = new ArrayList<>();
        infoDTOList = new ArrayList<>();

        imgnum = new Double[1];
        myLoc = new Double[2];

        imageContainer = rootView.findViewById(R.id.closet_image_container);
        first = rootView.findViewById(R.id.firstLinearLayout);

        searchText = rootView.findViewById(R.id.search);
        searchImage = rootView.findViewById(R.id.search_image);
        searchImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!searchText.getText().toString().equals("")) {
//                  edit text에 있는 string값과 같은 상품명을 확인해서 보여줌
                    check = SEARCH;
                } else {
                    check = NORMAL;
                }
                onStart();
            }
        });

        return rootView;
    }

    // Action Bar에 메뉴옵션 띄우기
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.actionbar_ourcloset, menu);
    }

    // Action Bar 메뉴옵션 선택 시
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int curId = item.getItemId();
        final Intent intent;

        switch (curId) {
            case R.id.actionbar_filter:
//                필터 옵션 메뉴 선택
//                필터 선택 후 My Closet 화면에 조건에 맞는 아이템을 보여줌
                intent = new Intent(getContext(), OurClosetFilterActivity.class);
                startActivityForResult(intent, REQUEST_FILTER);
                break;

            case R.id.actionbar_sorting:
                sortByDistance();
                check = SORTING;
                onStart();
                break;

            case R.id.actionbar_message:
                final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                String[] option = new String[]{"내가 보낸 메시지", "내가 받은 메시지"};
                builder.setItems(option, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int pos) {
                        if (pos == 0) {
                            Intent intent1 = new Intent(getContext(), RequestMessageActivity.class);
//                            Bundle bundle = new Bundle();
//                            bundle.putDouble("request", myDTO.getRequestNum());
//                            intent1.putExtras(bundle);
                            startActivity(intent1);
                        } else if (pos == 1) {
                            Intent intent1 = new Intent(getContext(), ResponseMessageActivity.class);
//                            Bundle bundle = new Bundle();
//                            bundle.putDouble("response", myDTO.getResponseNum());
//                            intent1.putExtras(bundle);
                            startActivity(intent1);
                        }
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (check == NORMAL) {
            infoDTOList.clear();

            totalUser();
        }
        accessDBInfo();
    }

    @Override
    public void onPause() {
        super.onPause();
        check = NORMAL;
    }

    private void totalUser() {

        db.collection("users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                totalNum++;
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void accessDBInfo() {
        if (check == NORMAL) {
            // 유저 정보접근
            db.collection("users").get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    checkNum++;
                                    accessUserInfoDB(document.getId());
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        } else {
            int count = addPathReference(check);
            floatTotalImages(count);
        }
    }

    private void accessUserInfoDB(final String document_id) {
        db
                .collection("users")
                .document(document_id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot result = task.getResult();
                            Map<String, Object> data = result.getData();

                            if (user.getUid().equals(document_id)) {
                                String address = (String) data.get("address");
                                String birthDay = (String) data.get("birthDay");
                                Double imgNum = (Double) data.get("imgNum");
                                Double latitude = (Double) data.get("latitude");
                                Double longitude = (Double) data.get("longitude");
                                Double lookNum = (Double) data.get("lookNum");
                                Double requestNum = (Double) data.get("requestNum");
                                Double responseNum = (Double) data.get("responseNum");
                                String name = (String) data.get("name");
                                String phoneNumber = (String) data.get("phoneNumber");
                                String userId = (String) data.get("userId");
                                myDTO = new UserInfo(userId, name, phoneNumber, birthDay, address,
                                        imgNum, lookNum, requestNum, responseNum, latitude, longitude);

                                myLoc[0] = (Double) data.get("latitude");
                                myLoc[1] = (Double) data.get("longitude");
                            } else {
                                String userId = (String) data.get("userId");
                                String address = (String) data.get("address");
                                Double latitude = (Double) data.get("latitude");
                                Double longitude = (Double) data.get("longitude");
                                String name = (String) data.get("name");
                                String age = (String) data.get("birthDay");
                                String phoneNumber = (String) data.get("phoneNumber");
                                Double responseNum = (Double) data.get("responseNum");
                                Double requestNum = (Double) data.get("requestNum");
                                final UserInfo userInfo = new UserInfo(userId, name, phoneNumber,
                                        address, latitude, longitude, responseNum, requestNum, age);

                                accessImageInfoDB(userInfo, document_id);
                            }
                        }
                    }
                });
    }

    private void accessImageInfoDB(final UserInfo userInfo, String document_id) {
        db
                .collection("images")
                .document(document_id)
                .collection("image")
                .whereEqualTo("shared", "공유")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String tempUrl = document.getString("imgURL");

                                Map<String, Object> temp = document.getData();

                                String id = (String) temp.get("userID");
                                String url = (String) temp.get("imgURL");
                                String category = (String) temp.get("category");
                                String name = (String) temp.get("itemName");
                                String color = (String) temp.get("color");
                                String brand = (String) temp.get("brand");
                                String season = (String) temp.get("season");
                                String size = (String) temp.get("size");
                                String shared = (String) temp.get("shared");
                                Double imgNum = (Double) temp.get("imgNum");
                                String price = (String) temp.get("price");
                                ImageDTO dto = new ImageDTO(id, url, category, name,
                                        color, brand, season, size, shared, price, imgNum);

                                infoDTOList.add(new InfoDTO(dto, userInfo));
                            }

                            if (totalNum == checkNum) {
                                int count = addPathReference(check);
                                floatTotalImages(count);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void sortByDistance() {
        ArrayList<Double> dist = new ArrayList<>();

        for (int i = 0; i < infoDTOList.size(); i++) {
            double distance = distance(myLoc[0], myLoc[1],
                    infoDTOList.get(i).getUserInfo().getLatitude(),
                    infoDTOList.get(i).getUserInfo().getLongitude());
            dist.add(distance);
        }

        for (int i = 0; i < dist.size() - 1; i++) {
            for (int j = i + 1; j < dist.size(); j++) {
                if (dist.get(i) > dist.get(j)) {
                    Collections.swap(dist, i, j);
                    Collections.swap(infoDTOList, i, j);
                }
            }
        }
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;

        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    private int addPathReference(int flag) {
        imageList.clear();
        userInfoList.clear();

        int count = 0;

        switch (flag) {
            case NORMAL:
            case SORTING:

                for (int i = 0; i < infoDTOList.size(); i++) {
                    count++;
                    imageList.add(storageRef.child(infoDTOList.get(i).getImageDTO().getImgURL()));
                    userInfoList.add(infoDTOList.get(i).getUserInfo());
                }

                break;

            case SEARCH:
                String sText = searchText.getText().toString();
                for (int i = 0; i < infoDTOList.size(); i++) {
                    if (sText.equals(infoDTOList.get(i).getImageDTO().getBrand()) ||
                            sText.equals(infoDTOList.get(i).getImageDTO().getItemName())) {
                        count++;
                        imageList.add(storageRef.child(infoDTOList.get(i).getImageDTO().getImgURL()));
                        userInfoList.add(infoDTOList.get(i).getUserInfo());
                    }
                }
                searchText.setText("");

                break;

            case FILTER:
                for (InfoDTO dto : filterList) {
                    count++;
                    imageList.add(storageRef.child(dto.getImageDTO().getImgURL()));
                    userInfoList.add(dto.getUserInfo());
                }

                break;
        }

        return count;
    }

    private void floatTotalImages(int count) {
        LinearLayout linearLayout = null;
        imageContainer.removeAllViews();

        int height = first.getHeight();

        int i = 0;
        while (i < count) {
            final int index = i;
            final StorageReference pathReference = imageList.get(i);

            if (i % 3 == 0) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, height);

                linearLayout = new LinearLayout(imageContainer.getContext());
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                linearLayout.setLayoutParams(layoutParams);

                imageContainer.addView(linearLayout);
            }

            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, height);
            imageParams.setMargins(5, 5, 5, 5);
            imageParams.gravity = Gravity.LEFT;
            imageParams.weight = 1;

            ImageView imageView = new ImageView(linearLayout.getContext());
            imageView.setLayoutParams(imageParams);

            Glide.with(linearLayout)
                    .load(pathReference)
                    .into(imageView);
            linearLayout.addView(imageView);

            i++;

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                    String[] option = {"정보 보기", "공유 요청 보내기"};
                    builder.setItems(option, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int pos) {
                            // "전화 걸기", "길찾기"
                            Intent intent;
                            Bundle bundle = new Bundle();
                            switch (pos) {
                                case 0:
                                    intent = new Intent(getContext(), ViewClosetInfoActivity.class);
                                    bundle.putString("price", infoDTOList.get(index).getImageDTO().getPrice());
                                    bundle.putString("name", infoDTOList.get(index).getImageDTO().getItemName());
                                    bundle.putString("category", infoDTOList.get(index).getImageDTO().getCategory());
                                    bundle.putString("color", infoDTOList.get(index).getImageDTO().getColor());
                                    bundle.putString("brand", infoDTOList.get(index).getImageDTO().getBrand());
                                    bundle.putString("season", infoDTOList.get(index).getImageDTO().getSeason());
                                    bundle.putString("size", infoDTOList.get(index).getImageDTO().getSize());
                                    bundle.putString("image", infoDTOList.get(index).getImageDTO().getImgURL());
                                    intent.putExtras(bundle);
                                    startActivity(intent);

                                    break;

                                case 1:
                                    intent = new Intent(getContext(), RequestActivity.class);
                                    bundle = new Bundle();

                                    bundle.putString("name", myDTO.getName());
                                    bundle.putString("age", myDTO.getBirthDay());
                                    bundle.putString("phone", myDTO.getPhoneNumber());
                                    bundle.putString("address", myDTO.getAddress());
                                    bundle.putDouble("latitude", myDTO.getLatitude());
                                    bundle.putDouble("longitude", myDTO.getLongitude());
                                    bundle.putDouble("request", myDTO.getRequestNum());

                                    bundle.putString("url", infoDTOList.get(index).getImageDTO().getImgURL());
                                    bundle.putDouble("response", infoDTOList.get(index).getUserInfo().getResponseNum());
                                    bundle.putString("nameB", infoDTOList.get(index).getUserInfo().getName());
                                    bundle.putString("ageB", infoDTOList.get(index).getUserInfo().getBirthDay());
                                    bundle.putString("phoneB", infoDTOList.get(index).getUserInfo().getPhoneNumber());
                                    bundle.putString("addressB", infoDTOList.get(index).getUserInfo().getAddress());
                                    bundle.putDouble("latitudeB", infoDTOList.get(index).getUserInfo().getLatitude());
                                    bundle.putDouble("longitudeB", infoDTOList.get(index).getUserInfo().getLongitude());
                                    bundle.putString("UIDB", infoDTOList.get(index).getUserInfo().getUserId());

                                    intent.putExtras(bundle);
                                    startActivity(intent);

                                    break;

                            }
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FILTER) {
            if (resultCode == -1) {
                Bundle bundle = data.getExtras();

                ArrayList<String> categoryItemList = bundle.getStringArrayList("category");
                ArrayList<String> colorItemList = bundle.getStringArrayList("color");
                ArrayList<String> seasonItemList = bundle.getStringArrayList("season");

                filterList.clear();
                filterList.addAll(filterCategory(infoDTOList, categoryItemList));
                HashSet<InfoDTO> temp1 = filterColor(filterList, colorItemList);
                if (temp1 != null) {
                    filterList.clear();
                    filterList.addAll(temp1);
                }
                HashSet<InfoDTO> temp2 = filterSeason(filterList, seasonItemList);
                if (temp2 != null) {
                    filterList.clear();
                    filterList.addAll(temp2);
                }

                if (filterList.size() == 0) {
                    check = NORMAL;
                    Toast.makeText(getContext(), "해당하는 값이 없습니다.", Toast.LENGTH_SHORT).show();
                } else {
                    check = FILTER;
                }
            }
        }
    }

    private ArrayList<InfoDTO> filterCategory
            (ArrayList<InfoDTO> list, ArrayList<String> arrayList) {
        ArrayList<InfoDTO> temp = new ArrayList<>();

        if (arrayList.size() == 0) {
            return list;
        } else {
            for (int i = 0; i < list.size(); i++) {
                for (int j = 0; j < arrayList.size(); j++) {
                    if (list.get(i).getImageDTO().getCategory().equals(arrayList.get(j))) {
                        temp.add(list.get((i)));
                        break;
                    }
                }
            }
        }

        return temp;
    }

    private HashSet<InfoDTO> filterColor
            (HashSet<InfoDTO> list, ArrayList<String> arrayList) {
        HashSet<InfoDTO> temp = new HashSet<>();

        if (arrayList.size() == 0) {
            return null;
        } else {
            for (InfoDTO dto : list) {
                String[] tempColor = dto.getImageDTO().getColor().split(" ");
                for (int k = 0; k < tempColor.length; k++) {
                    int flag = 0;
                    for (int j = 0; j < arrayList.size(); j++) {
                        if (tempColor[k].equals(arrayList.get(j))) {
                            temp.add(dto);
                            flag = 1;
                            break;
                        }
                    }

                    if (flag == 1) {
                        break;
                    }
                }
            }
        }

        return temp;
    }

    private HashSet<InfoDTO> filterSeason
            (HashSet<InfoDTO> list, ArrayList<String> arrayList) {
        HashSet<InfoDTO> temp = new HashSet<>();

        if (arrayList.size() == 0) {
            return null;
        } else {
            for (InfoDTO dto : list) {
                String[] temSeason = dto.getImageDTO().getSeason().split(" ");
                for (int k = 0; k < temSeason.length; k++) {
                    int flag = 0;
                    for (int j = 0; j < arrayList.size(); j++) {
                        if (temSeason[k].equals(arrayList.get(j))) {
                            temp.add(dto);
                            flag = 1;
                            break;
                        }
                    }

                    if (flag == 1) {
                        break;
                    }
                }
            }
        }

        return temp;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("test", "our closet onStop");
        imageContainer.removeAllViews();
    }
}
