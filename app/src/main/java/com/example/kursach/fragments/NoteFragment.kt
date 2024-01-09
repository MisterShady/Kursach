package com.example.kursach.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.kursach.databinding.FragmentNoteBinding
import com.example.kursach.viewmodels.NoteViewModel


class NoteFragment : Fragment() {

    private val args by navArgs<com.example.kursach.fragments.NoteFragmentArgs>()
    private val currentDay by lazy { args.currentDay }
    private val lesson by lazy { args.lesson }
    private val viewModel: NoteViewModel by viewModels { NoteViewModel.Factory(currentDay, lesson) }
    private var _binding: FragmentNoteBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNoteBinding.inflate(layoutInflater, container, false)

        binding.currentDayTextView.text = currentDay
        binding.lessonTextView.text = lesson

        viewModel.scheduleItem.observe(viewLifecycleOwner) {
            binding.editText.setText(it.note)
        }

        binding.saveButton.setOnClickListener {
            viewModel.saveScheduleItem(binding.editText.text.toString())
            val action =
                com.example.kursach.fragments.NoteFragmentDirections.actionNoteFragmentToMainFragment()
            findNavController().navigate(action)
        }

        binding.backButton.setOnClickListener {
            val action =
                com.example.kursach.fragments.NoteFragmentDirections.actionNoteFragmentToMainFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }


}