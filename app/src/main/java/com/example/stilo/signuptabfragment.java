package com.example.stilo;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class signuptabfragment extends Fragment {

    private static final String TAG = "SignupFragment";

    private List<String> listaEstados = new ArrayList<>();
    private Map<String, List<String>> mapaCidadesPorEstado = new HashMap<>();
    private boolean isAddressDataLoaded = false;

    private static final Map<String, String> ufToEstadoMap = new HashMap<>();
    static {
        ufToEstadoMap.put("AC", "Acre");
        ufToEstadoMap.put("AL", "Alagoas");
        ufToEstadoMap.put("AP", "Amapá");
        ufToEstadoMap.put("AM", "Amazonas");
        ufToEstadoMap.put("BA", "Bahia");
        ufToEstadoMap.put("CE", "Ceará");
        ufToEstadoMap.put("DF", "Distrito Federal");
        ufToEstadoMap.put("ES", "Espírito Santo");
        ufToEstadoMap.put("GO", "Goiás");
        ufToEstadoMap.put("MA", "Maranhão");
        ufToEstadoMap.put("MT", "Mato Grosso");
        ufToEstadoMap.put("MS", "Mato Grosso do Sul");
        ufToEstadoMap.put("MG", "Minas Gerais");
        ufToEstadoMap.put("PA", "Pará");
        ufToEstadoMap.put("PB", "Paraíba");
        ufToEstadoMap.put("PR", "Paraná");
        ufToEstadoMap.put("PE", "Pernambuco");
        ufToEstadoMap.put("PI", "Piauí");
        ufToEstadoMap.put("RJ", "Rio de Janeiro");
        ufToEstadoMap.put("RN", "Rio Grande do Norte");
        ufToEstadoMap.put("RS", "Rio Grande do Sul");
        ufToEstadoMap.put("RO", "Rondônia");
        ufToEstadoMap.put("RR", "Roraima");
        ufToEstadoMap.put("SC", "Santa Catarina");
        ufToEstadoMap.put("SP", "São Paulo");
        ufToEstadoMap.put("SE", "Sergipe");
        ufToEstadoMap.put("TO", "Tocantins");
    }

    private EditText nomeCompletoEditText, apelidoEditText, cpfEditText;
    private EditText razaoSocialEditText, nomeFantasiaEditText, cnpjEditText;
    private EditText emailEditText, passwordEditText, confirmPasswordEditText, dataNascimentoEditText, telefoneEditText;
    private AutoCompleteTextView estadoAutoComplete, cidadeAutoComplete;
    private EditText cepEditText, enderecoEditText, bairroEditText, numeroEditText;
    private AutoCompleteTextView estadoAutoComplete2, cidadeAutoComplete2;
    private EditText cepEditText2, enderecoEditText2, bairroEditText2, numeroEditText2;
    private TextInputLayout dataNascimentoLayout;
    private CheckBox termosCheckBox;
    private TextView termosTextView;
    private RadioGroup userTypeRadioGroup;
    private LinearLayout clienteFieldsLayout, prestadorFieldsLayout, prestadorAddressAdditionsLayout, additionalAddressContainer;
    private TextView enderecoPrincipalLabel;
    private Button signupButton, addAddressButton;
    private TextView goToLoginText;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private final Calendar myCalendar = Calendar.getInstance();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private VideoView videoView;

    public static final String FORMAT_CPF = "###.###.###-##";
    public static final String FORMAT_FONE_NUMEROS = "(##) #####-####";
    public static final String FORMAT_CNPJ = "##.###.###/####-##";
    public static final String FORMAT_CEP = "#####-###";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_signuptabfragment, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initViews(view);
        setupVideo(view);
        setupMasks();
        setupListeners();
        carregarEstadosECidadesDoJson();

        return view;
    }

    private void initViews(View view) {
        nomeCompletoEditText = view.findViewById(R.id.nomecompleto);
        apelidoEditText = view.findViewById(R.id.apelido);
        cpfEditText = view.findViewById(R.id.signup_cpf);
        razaoSocialEditText = view.findViewById(R.id.razaoSocial);
        nomeFantasiaEditText = view.findViewById(R.id.nomeFantasia);
        cnpjEditText = view.findViewById(R.id.signup_cnpj);
        emailEditText = view.findViewById(R.id.signup_email);
        passwordEditText = view.findViewById(R.id.signup_password);
        confirmPasswordEditText = view.findViewById(R.id.signup_confirm);
        dataNascimentoLayout = view.findViewById(R.id.data_nascimento_layout);
        dataNascimentoEditText = view.findViewById(R.id.data_nascimento);
        telefoneEditText = view.findViewById(R.id.telefone);
        estadoAutoComplete = view.findViewById(R.id.signup_estado);
        cidadeAutoComplete = view.findViewById(R.id.signup_cidade);
        termosCheckBox = view.findViewById(R.id.checkbox_termos);
        termosTextView = view.findViewById(R.id.text_termos_link);
        signupButton = view.findViewById(R.id.signup_button);
        goToLoginText = view.findViewById(R.id.text_go_to_login);
        userTypeRadioGroup = view.findViewById(R.id.radioGroupUserType);
        clienteFieldsLayout = view.findViewById(R.id.layoutClienteFields);
        prestadorFieldsLayout = view.findViewById(R.id.layoutPrestadorFields);
        cepEditText = view.findViewById(R.id.signup_cep);
        enderecoEditText = view.findViewById(R.id.signup_endereco);
        bairroEditText = view.findViewById(R.id.signup_bairro);
        numeroEditText = view.findViewById(R.id.signup_numero);

        cepEditText2 = view.findViewById(R.id.signup_cep_2);
        estadoAutoComplete2 = view.findViewById(R.id.signup_estado_2);
        cidadeAutoComplete2 = view.findViewById(R.id.signup_cidade_2);
        enderecoEditText2 = view.findViewById(R.id.signup_endereco_2);
        bairroEditText2 = view.findViewById(R.id.signup_bairro_2);
        numeroEditText2 = view.findViewById(R.id.signup_numero_2);

        enderecoPrincipalLabel = view.findViewById(R.id.endereco_principal_label);
        prestadorAddressAdditionsLayout = view.findViewById(R.id.prestador_address_additions);
        addAddressButton = view.findViewById(R.id.add_address_button);
        additionalAddressContainer = view.findViewById(R.id.additional_address_container);
    }

    private void setupVideo(View view) {
        if (getContext() == null) return;
        videoView = view.findViewById(R.id.videoView);
        String videoPath = "android.resource://" + getContext().getPackageName() + "/" + R.raw.stilocadastro;
        Uri uri = Uri.parse(videoPath);
        videoView.setVideoURI(uri);
        videoView.setOnPreparedListener(mp -> mp.setLooping(true));
        videoView.start();
    }

    private void setupMasks() {
        cpfEditText.addTextChangedListener(new MaskTextWatcher(cpfEditText, FORMAT_CPF));
        cnpjEditText.addTextChangedListener(new MaskTextWatcher(cnpjEditText, FORMAT_CNPJ));
        telefoneEditText.addTextChangedListener(new MaskTextWatcher(telefoneEditText, FORMAT_FONE_NUMEROS));
        cepEditText.addTextChangedListener(new MaskTextWatcher(cepEditText, FORMAT_CEP));
        cepEditText2.addTextChangedListener(new MaskTextWatcher(cepEditText2, FORMAT_CEP));
    }

    private void setupListeners() {
        userTypeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isPrestador = checkedId == R.id.radioButtonPrestador;
            clienteFieldsLayout.setVisibility(isPrestador ? View.GONE : View.VISIBLE);
            prestadorFieldsLayout.setVisibility(isPrestador ? View.VISIBLE : View.GONE);
            enderecoPrincipalLabel.setVisibility(isPrestador ? View.VISIBLE : View.GONE);
            prestadorAddressAdditionsLayout.setVisibility(isPrestador ? View.VISIBLE : View.GONE);
            dataNascimentoLayout.setHint(isPrestador ? "Data de Abertura" : "Data de Nascimento");
            if (isPrestador) {
                nomeCompletoEditText.setText("");
                apelidoEditText.setText("");
                cpfEditText.setText("");
            } else {
                razaoSocialEditText.setText("");
                nomeFantasiaEditText.setText("");
                cnpjEditText.setText("");
                additionalAddressContainer.setVisibility(View.GONE);
            }
        });

        addAddressButton.setOnClickListener(v -> additionalAddressContainer.setVisibility(additionalAddressContainer.getVisibility() == View.GONE ? View.VISIBLE : View.GONE));

        cepEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() == 9) {
                    buscarCep(s.toString(), estadoAutoComplete, cidadeAutoComplete, enderecoEditText, bairroEditText);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        cepEditText2.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().length() == 9) {
                    buscarCep(s.toString(), estadoAutoComplete2, cidadeAutoComplete2, enderecoEditText2, bairroEditText2);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        estadoAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                 atualizarCidades(s.toString(), cidadeAutoComplete);
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        estadoAutoComplete2.setOnItemClickListener((parent, view, position, id) -> {
            String estadoSelecionado = (String) parent.getItemAtPosition(position);
            atualizarCidades(estadoSelecionado, cidadeAutoComplete2);
        });

        signupButton.setOnClickListener(v -> performSignup());

        DatePickerDialog.OnDateSetListener date = (view1, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        };
        dataNascimentoEditText.setOnClickListener(v -> {
            if (getContext() != null) {
                new DatePickerDialog(getContext(), date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        goToLoginText.setOnClickListener(v -> {
            if (getActivity() instanceof AuthenticationActivity) {
                ((AuthenticationActivity) getActivity()).navigateToLogin();
            }
        });

        makeTermsTextClickable();
    }

    private void makeTermsTextClickable() {
        String text = getString(R.string.termos_de_uso_e_privacidade);
        SpannableString spannableString = new SpannableString(text);

        ClickableSpan termsClickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(getActivity(), LegalActivity.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(true);
                ds.setTypeface(Typeface.create(ds.getTypeface(), Typeface.BOLD));
                ds.setColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            }
        };

        int termsStart = text.indexOf("Termos de Uso");
        int termsEnd = termsStart + "Termos de Uso".length();
        spannableString.setSpan(termsClickableSpan, termsStart, termsEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        int privacyStart = text.indexOf("Acordo de Privacidade");
        int privacyEnd = privacyStart + "Acordo de Privacidade".length();
        spannableString.setSpan(termsClickableSpan, privacyStart, privacyEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        termosTextView.setText(spannableString);
        termosTextView.setMovementMethod(LinkMovementMethod.getInstance());
    }


    private void performSignup() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();
        String dob = dataNascimentoEditText.getText().toString().trim();
        String phoneRaw = MaskTextWatcher.unmask(telefoneEditText.getText().toString());
        String cep = MaskTextWatcher.unmask(cepEditText.getText().toString());
        String endereco = enderecoEditText.getText().toString().trim();
        String bairro = bairroEditText.getText().toString().trim();
        String numero = numeroEditText.getText().toString().trim();
        String estado = estadoAutoComplete.getText().toString().trim();
        String cidade = cidadeAutoComplete.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || dob.isEmpty() || phoneRaw.length() < 10) {
            Toast.makeText(getContext(), "Por favor, preencha os campos essenciais.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "As senhas não coincidem.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!termosCheckBox.isChecked()) {
            Toast.makeText(getContext(), "Você deve aceitar os Termos de Uso.", Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedId = userTypeRadioGroup.getCheckedRadioButtonId();
        String userType = selectedId == R.id.radioButtonCliente ? "cliente" : "prestador";

        String nomeOuRazao, apelidoOuFantasia, cpfOuCnpjRaw;

        if (userType.equals("cliente")) {
            nomeOuRazao = nomeCompletoEditText.getText().toString().trim();
            apelidoOuFantasia = apelidoEditText.getText().toString().trim();
            cpfOuCnpjRaw = MaskTextWatcher.unmask(cpfEditText.getText().toString().trim());
            if (nomeOuRazao.isEmpty() || cpfOuCnpjRaw.isEmpty()) {
                Toast.makeText(getContext(), "Nome completo e CPF são obrigatórios.", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            nomeOuRazao = razaoSocialEditText.getText().toString().trim();
            apelidoOuFantasia = nomeFantasiaEditText.getText().toString().trim();
            cpfOuCnpjRaw = MaskTextWatcher.unmask(cnpjEditText.getText().toString().trim());
            if (nomeOuRazao.isEmpty() || cpfOuCnpjRaw.isEmpty()) {
                Toast.makeText(getContext(), "Razão Social e CNPJ são obrigatórios.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        createUser(userType, nomeOuRazao, apelidoOuFantasia, cpfOuCnpjRaw, email, password, dob, phoneRaw, cep, endereco, bairro, numero, estado, cidade);
    }

    private void createUser(String userType, String nomeOuRazao, String apelidoOuFantasia, String cpfOuCnpjRaw, String email, String password, String dob, String phoneRaw, String cep, String endereco, String bairro, String numero, String estado, String cidade) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        user.sendEmailVerification().addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Toast.makeText(getContext(), "E-mail de verificação enviado.", Toast.LENGTH_SHORT).show();
                                saveUserDataToFirestore(user.getUid(), userType, nomeOuRazao, apelidoOuFantasia, cpfOuCnpjRaw, email, dob, phoneRaw, cep, endereco, bairro, numero, estado, cidade);
                                Intent intent = new Intent(getActivity(), VerifyEmailActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(getContext(), "Falha ao enviar e-mail de verificação.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    // Tratamento de erros de criação de usuário
                }
            });
    }

    private void saveUserDataToFirestore(String userId, String userType, String nomeOuRazao, String apelidoOuFantasia, String cpfOuCnpjRaw, String email, String dob, String phoneRaw, String cep, String endereco, String bairro, String numero, String estado, String cidade) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("userType", userType.toLowerCase());
        userData.put("email", email);
        userData.put("dateOfBirth", dob);
        userData.put("phone", phoneRaw);

        Map<String, Object> mainAddress = new HashMap<>();
        mainAddress.put("cep", cep);
        mainAddress.put("endereco", endereco);
        mainAddress.put("bairro", bairro);
        mainAddress.put("numero", numero);
        mainAddress.put("estado", estado);
        mainAddress.put("cidade", cidade);
        userData.put("address", mainAddress);

        if ("cliente".equals(userType.toLowerCase())) {
            userData.put("nomeCompleto", nomeOuRazao);
            userData.put("apelido", apelidoOuFantasia);
            userData.put("cpf", cpfOuCnpjRaw);
        } else {
            userData.put("razaoSocial", nomeOuRazao);
            userData.put("nomeFantasia", apelidoOuFantasia);
            userData.put("cnpj", cpfOuCnpjRaw);
            if (additionalAddressContainer.getVisibility() == View.VISIBLE) {
                 Map<String, Object> additionalAddress = new HashMap<>();
                additionalAddress.put("cep", MaskTextWatcher.unmask(cepEditText2.getText().toString()));
                additionalAddress.put("estado", estadoAutoComplete2.getText().toString());
                additionalAddress.put("cidade", cidadeAutoComplete2.getText().toString());
                additionalAddress.put("endereco", enderecoEditText2.getText().toString());
                additionalAddress.put("bairro", bairroEditText2.getText().toString());
                additionalAddress.put("numero", numeroEditText2.getText().toString());
                userData.put("additionalAddress", additionalAddress);
            }
        }

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Documento salvo com sucesso!"))
                .addOnFailureListener(e -> Log.w(TAG, "Erro ao salvar documento", e));
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("pt", "BR"));
        dataNascimentoEditText.setText(sdf.format(myCalendar.getTime()));
    }

    private void carregarEstadosECidadesDoJson() {
        if (getContext() == null) return;
        executorService.execute(() -> {
            try {
                InputStream is = getContext().getResources().openRawResource(R.raw.estadoscidades);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                is.close();

                JSONObject jsonObject = new JSONObject(sb.toString());
                JSONArray estadosJson = jsonObject.getJSONArray("estados");

                for (int i = 0; i < estadosJson.length(); i++) {
                    JSONObject estadoObj = estadosJson.getJSONObject(i);
                    String nomeEstado = estadoObj.getString("nome");
                    listaEstados.add(nomeEstado);

                    JSONArray cidadesJson = estadoObj.getJSONArray("cidades");
                    List<String> cidades = new ArrayList<>();
                    for (int j = 0; j < cidadesJson.length(); j++) {
                        cidades.add(cidadesJson.getString(j));
                    }
                    mapaCidadesPorEstado.put(nomeEstado, cidades);
                }

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        ArrayAdapter<String> adapterEstados = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, listaEstados);
                        estadoAutoComplete.setAdapter(adapterEstados);
                        estadoAutoComplete2.setAdapter(adapterEstados);
                        isAddressDataLoaded = true;
                    });
                }

            } catch (IOException | JSONException e) {
                Log.e(TAG, "Erro ao carregar estados e cidades", e);
            }
        });
    }

    private void buscarCep(String cep, AutoCompleteTextView estadoAutoComplete, AutoCompleteTextView cidadeAutoComplete, EditText enderecoEditText, EditText bairroEditText) {
        if (!isAddressDataLoaded) {
            if(getActivity() != null) {
                getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Aguarde o carregamento dos dados de endereço.", Toast.LENGTH_SHORT).show());
            }
            return;
        }

        String urlString = "https://viacep.com.br/ws/" + MaskTextWatcher.unmask(cep) + "/json/";

        executorService.execute(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                bufferedReader.close();

                JSONObject jsonObject = new JSONObject(stringBuilder.toString());

                if (jsonObject.has("erro")) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "CEP não encontrado.", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    String estadoNome = ufToEstadoMap.get(jsonObject.getString("uf"));
                    String cidadeNome = jsonObject.getString("localidade");
                    String logradouro = jsonObject.optString("logradouro");
                    String bairro = jsonObject.optString("bairro");

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (estadoNome != null) {
                                estadoAutoComplete.setText(estadoNome, false);
                                // A mágica acontece aqui! Ao setar o texto, o listener do estado será acionado
                                // e o atualizarCidades será chamado, populando o spinner de cidades.
                                // Depois disso, podemos setar a cidade.
                                cidadeAutoComplete.post(() -> cidadeAutoComplete.setText(cidadeNome, false));
                            }
                            if (enderecoEditText != null && logradouro != null && !logradouro.isEmpty()) {
                                enderecoEditText.setText(logradouro);
                            }
                            if (bairroEditText != null && bairro != null && !bairro.isEmpty()) {
                                bairroEditText.setText(bairro);
                            }
                        });
                    }
                }
            } catch (IOException | JSONException e) {
                Log.e(TAG, "Erro ao buscar CEP", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Falha ao buscar CEP.", Toast.LENGTH_SHORT).show());
                }
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        });
    }

    private void atualizarCidades(String estado, AutoCompleteTextView cidadeAutoComplete) {
        List<String> cidades = mapaCidadesPorEstado.get(estado);
        if (cidades != null && getContext() != null) {
            ArrayAdapter<String> adapterCidades = new ArrayAdapter<>(getContext(), android.R.layout.simple_dropdown_item_1line, cidades);
            cidadeAutoComplete.setAdapter(adapterCidades);
        } else {
            cidadeAutoComplete.setAdapter(null); // Limpa o adaptador se não houver cidades
        }
         cidadeAutoComplete.setText("", false); // Limpa o campo de cidade
    }

    // ... (outros métodos)
}
