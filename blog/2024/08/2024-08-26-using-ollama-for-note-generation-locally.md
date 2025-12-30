---
slug: using-ollama-for-note-generation-locally
title: 'Using Ollama for Note Generation Locally'
tags: ["llm", "data-science"]
authors: [raghu-vijaykumar]
date: 2024-08-26
description: "Using Ollama library to run and connect to models locally for generating readable and easy-to-understand notes."
---

![Using Ollama for Note Generation Locally](./assets/cover.png)

We will explore how to use the Ollama library to run and connect to models locally for generating readable and easy-to-understand notes. We will walk through the process of setting up the environment, running the code, and comparing the performance and quality of different models like llama3:8b, phi3:14b, llava:34b, and llama3:70b.

<!-- truncate -->

I generated notes from a transcript of a YouTube video in markdown format, with no changes in prompt, here I have included pdf versions.

- Transcript: https://github.com/raghu-vijaykumar/ollama-note-generation/blob/main/transcript/python_tutorial.raw.txt
- llama3:8b generated (ok): https://github.com/raghu-vijaykumar/ollama-note-generation/blob/main/transcript/python_tutorial.raw.llama3_8b.notes.pdf
- phi3:14b generated (better): https://github.com/raghu-vijaykumar/ollama-note-generation/blob/main/transcript/python_tutorial.raw.phi3_14b.notes.pdf
- llava:34b generated (bit descriptive, with additional info): https://github.com/raghu-vijaykumar/ollama-note-generation/blob/main/transcript/python_tutorial.raw.llava_34b.notes.pdf

## Step-by-Step Guide

First, we need to install Ollama on your system. Follow the instructions here https://ollama.com/download based on your operating system.

### Create a virtual environment

```sh
python -m venv venv
source venv/bin/activate
```

### Install the dependencies

```sh
pip install ollama
```

### Run the Model

Next, download and run the phi3:14b model using the Ollama library.

```sh
ollama run phi3:14b
```

### Generate Notes from Transcripts

Now, we will use the provided code to generate notes from .raw.txt files. Here are the scripts you need:

app.py is a Python script designed to execute various pipelines for generating notes from raw transcripts using the Ollama model. It utilizes the ThreadPoolExecutor for concurrent execution of tasks. The main functions include:

- process_raw_to_notes: Function to process raw transcript files into notes using the NotesGenerator class.
- run_raw_to_notes: Function to run the pipeline for converting raw transcript files to notes.
- main: Main function to parse command-line arguments and initiate the note generation process.

```python
from concurrent.futures import ThreadPoolExecutor
import glob
import os
import logging
import sys

from notes_generator import NotesGenerator

# Set up logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(threadName)s - %(message)s",
    handlers=[logging.StreamHandler()],
)

def process_raw_to_notes(file_path, notes_generator):
    try:
        notes_generator.process_transcript(file_path)
    except Exception as e:
        logging.error(f"Error processing file {file_path}: {e}")

def run_raw_to_notes(model, max_threads, folder):
    notes_generator = NotesGenerator(model=model, max_tokens=4096)

    # Get all transcript files from input directory
    raw_files = glob.glob(os.path.join(folder, "**/*.raw.txt"), recursive=True)
    # Initialize a list to store raw files without corresponding notes files
    filtered_raw_files = []

    # Iterate over the raw files
    for raw_file in raw_files:
        # Get the name of the raw file without the extension
        raw_file_name = os.path.splitext(os.path.basename(raw_file))[0]
        # Check if a corresponding notes file exists
        model_name = model.replace(":", "_")
        notes_file = os.path.join(
            os.path.dirname(raw_file), raw_file_name + f".{model_name}" + ".notes.md"
        )
        if not os.path.exists(notes_file):
            # If notes file does not exist, add the raw file to the filtered list
            filtered_raw_files.append(raw_file)
    with ThreadPoolExecutor(max_workers=max_threads) as executor:
        futures = [
            executor.submit(process_raw_to_notes, file, notes_generator)
            for file in filtered_raw_files
        ]
        for future in futures:
            try:
                future.result()
            except Exception as e:
                logging.error(f"Error in transcript to notes thread: {e}")

def main(pipeline, model, max_threads, folder):
    if pipeline == "raw_to_notes":
        run_raw_to_notes(model, max_threads, folder)
    else:
        logging.error(f"Unknown pipeline: {pipeline}")
        sys.exit(1)

if __name__ == "__main__":
    if len(sys.argv) != 5:
        print("Usage: python main.py <pipeline> <max_threads> <model> <folder>")
        print("pipeline: 'raw_to_notes'")
        sys.exit(1)

    pipeline = sys.argv[1]
    max_threads = int(sys.argv[2])
    model = sys.argv[3]
    folder = sys.argv[4]

    logging.info("Starting note generation process.")
    main(pipeline, model, max_threads, folder)
    logging.info("Completed note generation process.")
```

notes_generator.py contains the NotesGenerator class responsible for generating notes from transcript files. Key functionalities of this class include:

- **init**: Initializes the NotesGenerator with the specified model and maximum tokens.
- split_text: Splits the transcript into chunks based on the maximum token limit.
- query_gpt: Queries the Ollama model to generate notes based on provided prompts.
- process_transcript: Reads a transcript file, splits it into chunks, and generates notes for each chunk.

```python
import logging
import os
import time
import ollama

class NotesGenerator:
    def __init__(self, model, max_tokens=512):
        self.model = model
        self.max_tokens = max_tokens
        self.system = """You are NotesGPT, When provided with a topic your task is
        - Taking detailed, precise, and easy-to-understand notes
        - Create advanced bullet-point notes summarizing the important parts of the reading or topic.
        - Include all essential information, use text highlighting with bold fonts for important key words.
        - Remove any extraneous language.
        - Strictly base your notes on the provided information.
        - Tabulate any comparisions in markdown syntax.
        - Numerical values in the context are important dont leave them out.
        - Includes code.
        - Use latex for any mathematical equations.
        - Avoid repetition.
        - The length of the summary should be appropriate for the length and complexity of the original text.
        - Dont include tasks or insructions or homework in the text.
        - Provide response in markdown for easy documentation.

        Content:
        """

    @staticmethod
    def count_tokens(text):
        """Counts the number of tokens in a text string."""
        return len(text.split())

    def split_text(self, text):
        """Splits the text into chunks based on a specified maximum number of tokens."""
        paragraphs = text.split("\n\n")
        chunks = []

        logging.info("Starting to split the transcript into chunks.")

        for paragraph in paragraphs:
            words = paragraph.split()
            while words:
                if len(words) <= self.max_tokens:
                    # Add the entire paragraph as a chunk
                    chunks.append(" ".join(words).strip())
                    logging.info(
                        f"Created a chunk with {self.count_tokens(' '.join(words))} tokens."
                    )
                    words = []
                else:
                    # Split the paragraph into a chunk and update remaining words
                    split_point = self.max_tokens
                    sub_chunk = words[:split_point]
                    chunks.append(" ".join(sub_chunk).strip())
                    logging.info(
                        f"Split a paragraph into a chunk with {self.count_tokens(' '.join(sub_chunk))} tokens."
                    )
                    words = words[split_point:]

        logging.info(f"Total chunks created: {len(chunks)}")
        return chunks

    def query_gpt(self, messages):
        """Generates notes for a given prompt using LLaMA3."""
        start_time = time.time()
        response = ollama.chat(model=self.model, messages=messages)
        end_time = time.time()
        logging.info(
            f"Received response of {len(response['message']['content'])} tokens for input {len(messages[-1]['content'])} tokens from the model in {end_time - start_time:.2f} seconds."
        )
        return response["message"]

    def process_transcript(self, file_path):
        """Reads a transcript file, splits it, and generates notes."""
        logging.info(f"Reading transcript from {file_path}.")
        with open(file_path, "r", encoding="utf-8") as file:
            transcript = file.read()
        start_time = time.time()
        chunks = self.split_text(transcript)

        # Determine output path for notes
        model_name = self.model.replace(":", "_")
        output_path = os.path.splitext(file_path)[0] + f".{model_name}" + ".notes.md"

        messages = []
        with open(output_path, "w", encoding="utf-8") as output_file:
            for i, chunk in enumerate(chunks):
                logging.info(f"Processing chunk {i+1}/{len(chunks)}.")
                messages.append({"role": "user", "content": f"{self.system + chunk}"})
                message = self.query_gpt(messages)
                messages.append(message)
                output_file.write(message["content"] + "\n\n")
                output_file.flush()  # Ensure the note is written to disk immediately

        end_time = time.time()
        logging.info(
            f"Finished processing all chunks in {end_time - start_time:.2f} seconds."
        )
```

### Run the Script

Run the script to generate notes:

```sh
python app.py raw_to_notes 4 phi3_14b /path/to/your/folder
```

## Performance Comparison

We compared the note generation performance between different models:

- llama3:8b: Takes approximately 830 seconds to process, with decent quality.
- phi3:14b: Takes approximately 4200 seconds (1.5 hours) but produces better quality notes.
- llava:34b: Takes approximately 20000 seconds (5-6 Hours) its a bit descriptive for my taste, and provides info on my prompts (unnecessary overhead for automated generation, could be handled with better prompts).

You can also experiment with larger models like llama3:70b to see if the quality improves further and speeds depends on the GPU's.

## Conclusion

In conclusion, the utilization of Ollama for generating notes from raw transcripts offers a powerful solution for automating the note-taking process. By leveraging advanced language models like LLaMA3 and Phi3:14b, users can obtain detailed and accurate notes in a fraction of the time it would take to manually transcribe and summarize content. The ability to customize prompts and iterate on generated output allows for fine-tuning and refinement to achieve desired results.

Furthermore, the implementation of efficient chunking strategies in the note generation process enhances productivity and scalability. By breaking down large transcripts into manageable chunks, the system can process information more effectively and generate coherent notes without overwhelming computational resources.

Overall, Ollama's note generation capabilities, coupled with efficient chunking techniques, empower users to streamline their workflow, save time, and obtain high-quality notes effortlessly. However there are cases of spelling mistakes, passage repetitions, improper formatting even though specifically asked for markdown format and hallucinations, a manual review and revision is always needed.

Let me know if there are any other changes you'd like to incorporate!

[Source Code](https://github.com/raghu-vijaykumar/ollama-note-generation/tree/main)
