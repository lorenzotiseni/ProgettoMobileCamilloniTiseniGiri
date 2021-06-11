package com.example.progettomobilecamillonitisenigiri.Corso

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.progettomobilecamillonitisenigiri.Adapters.DomandeForumAdapter
import com.example.progettomobilecamillonitisenigiri.Model.DomandaForum
import com.example.progettomobilecamillonitisenigiri.Model.RispostaForum
import com.example.progettomobilecamillonitisenigiri.R
import com.example.progettomobilecamillonitisenigiri.ViewModels.FirebaseConnection
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class FragmentForumCorso : Fragment(), DomandeForumAdapter.OnDomandeAdapterListener {
    val firebaseConnection:FirebaseConnection by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_forum_corso, container, false)
    }


    override fun onResume() {
        super.onResume()
        val id = requireActivity().intent.getStringExtra("ID_CORSO").toString()
        //Aggiunta Nuova Domanda
        view?.findViewById<Button>(R.id.ButtonForum)?.setOnClickListener{
            val alertDialogAdd = AlertDialog.Builder(context)
            val input = EditText(context)
            input.inputType = InputType.TYPE_CLASS_TEXT
            alertDialogAdd.setView(input)
            alertDialogAdd.setTitle("Aggiungi una nuova  domanda")
            alertDialogAdd.setMessage("Aggiungendo una nuova domanda gli altri utenti potranno rispondere ad essa con dei commenti. Fare domande rende la fruizione del corso più facile per tutti!")
            alertDialogAdd.setPositiveButton(
                "AGGIUNGI",
                DialogInterface.OnClickListener() { dialog, which ->
                    val domanda = DomandaForum(firebaseConnection.getUser().value!!.firstName,firebaseConnection.getUser().value!!.lastName,firebaseConnection.newDomandaId(id),input.text.toString(), ArrayList<RispostaForum>())
                    if(!firebaseConnection.addDomanda(domanda,id)){
                        val contextView = requireView().findViewById<View>(R.id.fragmentforum)
                        Snackbar.make(contextView,"Domanda già esistente",Snackbar.LENGTH_SHORT).show()
                    }
                })
            alertDialogAdd.setNegativeButton("ANNULLA", null)
            alertDialogAdd.show()
        }

        val rvDomande: RecyclerView? = view?.findViewById(R.id.recyclerViewDomanda)

        rvDomande?.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL,
            false
        )



        firebaseConnection.getListDomande().observe(
            viewLifecycleOwner,
            Observer<HashMap<String, ArrayList<DomandaForum>>> { domande ->
                rvDomande?.adapter = DomandeForumAdapter(
                    domande.getValue(id).toList(),
                    context,
                    this,
                    this
                )
                //Abilita/disabilita la sezione a seconda dell'iscrizione dell'utente
                if (!firebaseConnection.isIscritto(id)) {
                    view?.findViewById<ConstraintLayout>(R.id.paginaNonVisualizzabile)?.visibility =
                        View.VISIBLE
                    view?.findViewById<ConstraintLayout>(R.id.paginaVisualizzabileIscritti)?.visibility =
                        View.GONE
                }
            })
    }

    override fun onDomandeClick(position: Int, view: View?) {
        val image = view?.findViewById<ImageView>(R.id.immagineDomande)

        val layout = view?.findViewById<ConstraintLayout>(R.id.expandableLayoutDomande)
        if (layout?.visibility == View.GONE) {
            image?.rotation = 90f
            layout?.setVisibility(View.VISIBLE)

        } else {
            image?.rotation = 0f
            layout?.setVisibility(View.GONE)
        }
    }
    fun addRispostaFrag(risposta:String, idDomanda:Int){
        val id = requireActivity().intent.getStringExtra("ID_CORSO").toString()
        val risposta = RispostaForum(firebaseConnection.getUser().value!!.firstName,firebaseConnection.getUser().value!!.lastName,risposta)
        if(!firebaseConnection.addRisposta(risposta,id,idDomanda)) {
            val contextView = requireView().findViewById<View>(R.id.fragmentforum)
            Snackbar.make(contextView, "La risposta esiste già", Snackbar.LENGTH_SHORT).show()
        }
    }
}