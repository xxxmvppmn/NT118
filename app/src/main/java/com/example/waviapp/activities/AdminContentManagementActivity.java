package com.example.waviapp.activities;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.waviapp.R;
import com.example.waviapp.adapters.AdminGrammarAdapter;
import com.example.waviapp.adapters.AdminVocabularyAdapter;
import com.example.waviapp.firebase.DatabaseHelper;
import com.example.waviapp.models.NguPhap;
import com.example.waviapp.models.TuVung;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminContentManagementActivity extends BaseAdminActivity implements
        AdminVocabularyAdapter.OnVocabActionListener,
        AdminGrammarAdapter.OnGrammarActionListener {

    private TabLayout tabLayout;
    private RecyclerView rvVocabulary, rvGrammar;
    private TextView txtEmpty;
    private FloatingActionButton fabAdd;
    private SwipeRefreshLayout swipeRefresh;

    private AdminVocabularyAdapter vocabAdapter;
    private AdminGrammarAdapter grammarAdapter;
    private DatabaseHelper dbHelper;

    private List<TuVung> allVocabulary = new ArrayList<>();
    private List<NguPhap> allGrammar = new ArrayList<>();

    private boolean isVocabLoaded = false;
    private boolean isGrammarLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_content_management);

        dbHelper = new DatabaseHelper();
        initViews();
        setupRecyclerViews();
        setupTabLayout();
        loadVocabulary(); // Initial lazy load (only vocabulary, tab 0)
    }

    private void initViews() {
        tabLayout = findViewById(R.id.tabLayout);
        rvVocabulary = findViewById(R.id.rvVocabulary);
        rvGrammar = findViewById(R.id.rvGrammar);
        txtEmpty = findViewById(R.id.txtEmpty);
        fabAdd = findViewById(R.id.fabAdd);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        fabAdd.setOnClickListener(v -> {
            if (tabLayout.getSelectedTabPosition() == 0) {
                showVocabDialog(null, -1);
            } else {
                showGrammarDialog(null, -1);
            }
        });

        swipeRefresh.setColorSchemeResources(R.color.admin_primary);
        swipeRefresh.setOnRefreshListener(() -> {
            if (tabLayout.getSelectedTabPosition() == 0) {
                loadVocabulary();
            } else {
                loadGrammar();
            }
        });
    }

    private void setupRecyclerViews() {
        // Vocab
        vocabAdapter = new AdminVocabularyAdapter(this);
        rvVocabulary.setLayoutManager(new LinearLayoutManager(this));
        rvVocabulary.setAdapter(vocabAdapter);

        // Grammar
        grammarAdapter = new AdminGrammarAdapter(this);
        rvGrammar.setLayoutManager(new LinearLayoutManager(this));
        rvGrammar.setAdapter(grammarAdapter);
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    rvVocabulary.setVisibility(View.VISIBLE);
                    rvGrammar.setVisibility(View.GONE);
                    updateEmptyView(allVocabulary.isEmpty());
                    if (!isVocabLoaded) {
                        loadVocabulary();
                    }
                } else {
                    rvVocabulary.setVisibility(View.GONE);
                    rvGrammar.setVisibility(View.VISIBLE);
                    updateEmptyView(allGrammar.isEmpty());
                    if (!isGrammarLoaded) {
                        loadGrammar();
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadVocabulary() {
        swipeRefresh.setRefreshing(true);
        dbHelper.getAllVocabulary(new DatabaseHelper.VocabularyCallback() {
            @Override
            public void onSuccess(List<TuVung> words) {
                if (isFinishing() || isDestroyed()) return;
                swipeRefresh.setRefreshing(false);
                isVocabLoaded = true;
                allVocabulary = words;
                vocabAdapter.setWords(words);
                if (tabLayout.getSelectedTabPosition() == 0) {
                    updateEmptyView(words.isEmpty());
                }
            }

            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                swipeRefresh.setRefreshing(false);
                Toast.makeText(AdminContentManagementActivity.this, "Lỗi tải từ vựng: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGrammar() {
        swipeRefresh.setRefreshing(true);
        dbHelper.getAllGrammar(new DatabaseHelper.GrammarCallback() {
            @Override
            public void onSuccess(List<NguPhap> grammarList) {
                if (isFinishing() || isDestroyed()) return;
                swipeRefresh.setRefreshing(false);
                isGrammarLoaded = true;
                allGrammar = grammarList;
                grammarAdapter.setGrammarList(grammarList);
                if (tabLayout.getSelectedTabPosition() == 1) {
                    updateEmptyView(grammarList.isEmpty());
                }
            }

            @Override
            public void onFailure(String error) {
                if (isFinishing() || isDestroyed()) return;
                swipeRefresh.setRefreshing(false);
                Toast.makeText(AdminContentManagementActivity.this, "Lỗi tải ngữ pháp: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmptyView(boolean isEmpty) {
        txtEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
    }

    // ================= VOCABULARY CRUD =================

    @Override
    public void onEdit(TuVung word) {
        // Find position of the word
        int pos = allVocabulary.indexOf(word);
        showVocabDialog(word, pos);
    }

    @Override
    public void onDelete(TuVung word, int position) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_delete)
                .setMessage("Bạn có chắc chắn muốn xóa từ vựng này không?")
                .setPositiveButton(R.string.admin_delete, (dialog, which) -> {
                    swipeRefresh.setRefreshing(true);
                    dbHelper.deleteVocabulary(word.getMaTV(), new DatabaseHelper.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            if (isFinishing() || isDestroyed()) return;
                            swipeRefresh.setRefreshing(false);
                            allVocabulary.remove(word);
                            vocabAdapter.removeItem(position);
                            if (tabLayout.getSelectedTabPosition() == 0) {
                                updateEmptyView(allVocabulary.isEmpty());
                            }
                            Toast.makeText(AdminContentManagementActivity.this, "Đã xóa từ vựng", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String error) {
                            if (isFinishing() || isDestroyed()) return;
                            swipeRefresh.setRefreshing(false);
                            Toast.makeText(AdminContentManagementActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.admin_cancel, null)
                .show();
    }

    private void showVocabDialog(TuVung existingWord, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_vocabulary, null);

        EditText edtCode = view.findViewById(R.id.edtVocabCode);
        EditText edtTopic = view.findViewById(R.id.edtVocabTopic);
        EditText edtEnglish = view.findViewById(R.id.edtVocabEnglish);
        EditText edtVietnamese = view.findViewById(R.id.edtVocabVietnamese);
        EditText edtType = view.findViewById(R.id.edtVocabType);
        EditText edtPhonetic = view.findViewById(R.id.edtVocabPhonetic);
        EditText edtLevel = view.findViewById(R.id.edtVocabLevel);

        boolean isEdit = existingWord != null;
        if (isEdit) {
            edtCode.setText(existingWord.getMaTV());
            edtCode.setEnabled(false);
            edtTopic.setText(existingWord.getMaCD());
            edtEnglish.setText(existingWord.getTuTiengAnh());
            edtVietnamese.setText(existingWord.getNghiaTiengViet());
            edtType.setText(existingWord.getLoaiTu());
            edtPhonetic.setText(existingWord.getPhienAm());
            edtLevel.setText(existingWord.getLevel());
        }

        builder.setView(view)
                .setTitle(isEdit ? "Chỉnh sửa từ vựng" : "Thêm từ vựng")
                .setPositiveButton(isEdit ? "Cập nhật" : "Lưu", null)
                .setNegativeButton("Hủy", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String code = edtCode.getText().toString().trim();
            String topic = edtTopic.getText().toString().trim();
            String english = edtEnglish.getText().toString().trim();
            String vietnamese = edtVietnamese.getText().toString().trim();
            String type = edtType.getText().toString().trim();
            String phonetic = edtPhonetic.getText().toString().trim();
            String level = edtLevel.getText().toString().trim();

            if (code.isEmpty() || topic.isEmpty() || english.isEmpty() || vietnamese.isEmpty() || type.isEmpty()) {
                Toast.makeText(AdminContentManagementActivity.this, "Vui lòng điền các trường bắt buộc!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check duplicate ID code for new items
            if (!isEdit) {
                for (TuVung w : allVocabulary) {
                    if (code.equalsIgnoreCase(w.getMaTV())) {
                        Toast.makeText(AdminContentManagementActivity.this, "Mã từ vựng đã tồn tại! Vui lòng nhập mã khác.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }

            swipeRefresh.setRefreshing(true);
            if (isEdit) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("maCD", topic);
                updates.put("tuTiengAnh", english);
                updates.put("nghiaTiengViet", vietnamese);
                updates.put("loaiTu", type);
                updates.put("phienAm", phonetic);
                updates.put("level", level);

                dbHelper.updateVocabulary(existingWord.getMaTV(), updates, new DatabaseHelper.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        if (isFinishing() || isDestroyed()) return;
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(AdminContentManagementActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                        
                        existingWord.setMaCD(topic);
                        existingWord.setTuTiengAnh(english);
                        existingWord.setNghiaTiengViet(vietnamese);
                        existingWord.setLoaiTu(type);
                        existingWord.setPhienAm(phonetic);
                        existingWord.setLevel(level);

                        if (position >= 0) {
                            vocabAdapter.updateItem(existingWord, position);
                        } else {
                            loadVocabulary();
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        if (isFinishing() || isDestroyed()) return;
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(AdminContentManagementActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                TuVung newWord = new TuVung(code, topic, english, vietnamese, type, phonetic, level);
                dbHelper.addVocabulary(newWord, new DatabaseHelper.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        if (isFinishing() || isDestroyed()) return;
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(AdminContentManagementActivity.this, "Thêm từ vựng thành công", Toast.LENGTH_SHORT).show();
                        loadVocabulary();
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        if (isFinishing() || isDestroyed()) return;
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(AdminContentManagementActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    // ================= GRAMMAR CRUD =================

    @Override
    public void onEdit(NguPhap grammar) {
        int pos = allGrammar.indexOf(grammar);
        showGrammarDialog(grammar, pos);
    }

    @Override
    public void onDelete(NguPhap grammar, int position) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.admin_delete)
                .setMessage("Bạn có chắc chắn muốn xóa bài ngữ pháp này không?")
                .setPositiveButton(R.string.admin_delete, (dialog, which) -> {
                    swipeRefresh.setRefreshing(true);
                    dbHelper.deleteGrammar(grammar.getMaNP(), new DatabaseHelper.SimpleCallback() {
                        @Override
                        public void onSuccess() {
                            if (isFinishing() || isDestroyed()) return;
                            swipeRefresh.setRefreshing(false);
                            allGrammar.remove(grammar);
                            grammarAdapter.removeItem(position);
                            if (tabLayout.getSelectedTabPosition() == 1) {
                                updateEmptyView(allGrammar.isEmpty());
                            }
                            Toast.makeText(AdminContentManagementActivity.this, "Đã xóa ngữ pháp", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(String error) {
                            if (isFinishing() || isDestroyed()) return;
                            swipeRefresh.setRefreshing(false);
                            Toast.makeText(AdminContentManagementActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton(R.string.admin_cancel, null)
                .show();
    }

    private void showGrammarDialog(NguPhap existingGrammar, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_grammar, null);

        EditText edtCode = view.findViewById(R.id.edtGrammarCode);
        EditText edtTopic = view.findViewById(R.id.edtGrammarTopic);
        EditText edtName = view.findViewById(R.id.edtGrammarName);
        EditText edtTheory = view.findViewById(R.id.edtGrammarTheory);
        EditText edtExample = view.findViewById(R.id.edtGrammarExample);
        EditText edtOrder = view.findViewById(R.id.edtGrammarOrder);

        boolean isEdit = existingGrammar != null;
        if (isEdit) {
            edtCode.setText(existingGrammar.getMaNP());
            edtCode.setEnabled(false);
            edtTopic.setText(existingGrammar.getMaCD());
            edtName.setText(existingGrammar.getTenBai());
            edtTheory.setText(existingGrammar.getNoiDungLyThuyet());
            edtExample.setText(existingGrammar.getViDu());
            edtOrder.setText(String.valueOf(existingGrammar.getOrder()));
        }

        builder.setView(view)
                .setTitle(isEdit ? "Chỉnh sửa ngữ pháp" : "Thêm ngữ pháp")
                .setPositiveButton(isEdit ? "Cập nhật" : "Lưu", null)
                .setNegativeButton("Hủy", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String code = edtCode.getText().toString().trim();
            String topic = edtTopic.getText().toString().trim();
            String name = edtName.getText().toString().trim();
            String theory = edtTheory.getText().toString().trim();
            String example = edtExample.getText().toString().trim();
            String orderStr = edtOrder.getText().toString().trim();

            if (code.isEmpty() || topic.isEmpty() || name.isEmpty() || theory.isEmpty()) {
                Toast.makeText(AdminContentManagementActivity.this, "Vui lòng điền các trường bắt buộc!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (orderStr.isEmpty()) {
                Toast.makeText(AdminContentManagementActivity.this, "Vui lòng nhập thứ tự sắp xếp!", Toast.LENGTH_SHORT).show();
                return;
            }

            int order;
            try {
                order = Integer.parseInt(orderStr);
            } catch (NumberFormatException e) {
                Toast.makeText(AdminContentManagementActivity.this, "Vui lòng nhập thứ tự là một số nguyên hợp lệ (cho phép 0)!", Toast.LENGTH_LONG).show();
                return;
            }

            // Check duplicate ID code for new items
            if (!isEdit) {
                for (NguPhap np : allGrammar) {
                    if (code.equalsIgnoreCase(np.getMaNP())) {
                        Toast.makeText(AdminContentManagementActivity.this, "Mã ngữ pháp đã tồn tại! Vui lòng nhập mã khác.", Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }

            swipeRefresh.setRefreshing(true);
            if (isEdit) {
                Map<String, Object> updates = new HashMap<>();
                updates.put("maCD", topic);
                updates.put("tenBai", name);
                updates.put("noiDungLyThuyet", theory);
                updates.put("viDu", example);
                updates.put("order", order);

                dbHelper.updateGrammar(existingGrammar.getMaNP(), updates, new DatabaseHelper.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        if (isFinishing() || isDestroyed()) return;
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(AdminContentManagementActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();

                        existingGrammar.setMaCD(topic);
                        existingGrammar.setTenBai(name);
                        existingGrammar.setNoiDungLyThuyet(theory);
                        existingGrammar.setViDu(example);
                        existingGrammar.setOrder(order);

                        if (position >= 0) {
                            grammarAdapter.updateItem(existingGrammar, position);
                        } else {
                            loadGrammar();
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        if (isFinishing() || isDestroyed()) return;
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(AdminContentManagementActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                NguPhap newGrammar = new NguPhap(code, topic, name, theory, example, order);
                dbHelper.addGrammar(newGrammar, new DatabaseHelper.SimpleCallback() {
                    @Override
                    public void onSuccess() {
                        if (isFinishing() || isDestroyed()) return;
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(AdminContentManagementActivity.this, "Thêm ngữ pháp thành công", Toast.LENGTH_SHORT).show();
                        loadGrammar();
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        if (isFinishing() || isDestroyed()) return;
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(AdminContentManagementActivity.this, "Lỗi: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
