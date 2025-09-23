package com.example.teladecadastro;

import android.os.Bundle;
import android.util.Log; // Importe Log
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
// Remova as importações de TabLayout e ViewPager2 se não forem mais usadas nesta Activity
// import androidx.viewpager2.widget.ViewPager2;
// import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity { // Ou renomeie para AuthenticationActivity

    private static final String TAG = "AuthActivity"; // TAG para logs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Não precisamos de EdgeToEdge.enable(this); para esta abordagem simples,
        // a menos que você tenha um motivo específico para mantê-lo.
        // Remova também o listener de WindowInsets se não for necessário aqui.

        setContentView(R.layout.activity_main); // Deve ser o layout com o FrameLayout (R.id.fragment_container)

        if (savedInstanceState == null) {
            // Exibe o LoginFragment (logintabfragment) inicialmente
            Log.d(TAG, "onCreate: Carregando logintabfragment inicial.");
            loadFragment(new logintabfragment(), false); // false para não adicionar à backstack
        }
    }

    private void loadFragment(Fragment fragment, boolean addToBackStack) {
        Log.d(TAG, "loadFragment: Carregando " + fragment.getClass().getSimpleName() + ", addToBackStack: " + addToBackStack);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Animações de transição (opcional, mas melhora a UX)
        fragmentTransaction.setCustomAnimations(
                android.R.anim.slide_in_left,  // Entra
                android.R.anim.slide_out_right, // Sai (fragmento antigo)
                android.R.anim.slide_in_left,  // Entra (ao voltar na backstack)
                android.R.anim.slide_out_right  // Sai (ao voltar na backstack)
        );

        fragmentTransaction.replace(R.id.fragment_container, fragment);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null); // Permite voltar com o botão "voltar" do dispositivo
        }
        fragmentTransaction.commit();
        Log.d(TAG, "loadFragment: Transação commitada.");
    }

    // Método chamado pelo LoginFragment para ir para a tela de Cadastro
    public void navigateToSignup() {
        Log.d(TAG, "navigateToSignup: Navegando para signuptabfragment.");
        loadFragment(new signuptabfragment(), true); // true para adicionar à backstack
    }

    // Método chamado pelo SignupFragment para voltar para a tela de Login
    public void navigateToLogin() {
        Log.d(TAG, "navigateToLogin: Navegando para logintabfragment.");
        // Se você quer limpar a backstack ao voltar para o login (para que o botão voltar não leve ao signup de novo)
        // getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        // loadFragment(new logintabfragment(), false);

        // Ou, se o SignupFragment foi adicionado à backstack, simplesmente pop:
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            // Caso raro, se de alguma forma a backstack estiver vazia, carrega o login
            loadFragment(new logintabfragment(), false);
        }
        // Se você sempre quer apenas substituir e não se preocupar com a pilha exata:
        // loadFragment(new logintabfragment(), false);
    }
}
