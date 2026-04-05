# Bloomix: A Multimodal Mood Tracking & Journaling Application

Bloomix is a research-driven mobile application developed to address the limitations of traditional mood trackers that often oversimplify human emotions into a single static state. By utilizing a "flower garden" metaphor, the app allows users to log multiple moods that "bloom" into a personalized, symbolic visualization of their emotional well-being.

---

## 🌟 Key Features
* **Multi-Mood Input:** Allows users to select multiple emotions and quantify their intensity for a more accurate daily representation.
* **AI-Powered Journaling:** Integrates Natural Language Processing (NLP) to analyze unstructured text for sentiment and emotional cues.
* **Actionable Insights:** Generates tailored reflection prompts and "micro-actions" based on predicted emotional states to support mental wellness.
* **Historical Analytics:** Provides monthly statistical representations of mood patterns and journaling frequency to foster self-awareness.

---

## 🛠️ Technical Stack
* **Mobile Development:** Kotlin & Android Studio.
* **Backend:** Firebase for secure data management and synchronization.
* **Design:** Figma and Canva for UI/UX and asset creation.
* **Machine Learning:** Hybrid implementation using Support Vector Machines (SVM) and Naïve Bayes.

---

## 🧠 Machine Learning Architecture
The Bloomix system follows a pipeline-based architecture to transform qualitative emotional data into visual and reflective outputs.

### 1. Text Preprocessing
Raw journal entries undergo a rigorous normalization process to improve classification accuracy:
* **Tokenization:** Splitting text into individual tokens and removing noise.
* **Negation Handling:** Detecting and merging negation terms (e.g., "not happy" $\rightarrow$ `not_happy`) to preserve context.
* **Porter Stemming:** Reducing words to their root forms to group similar terms.

### 2. Classification Models
* **Naïve Bayes (Sentiment Analysis):** Determines sentiment polarity (Positive, Negative, or Neutral) using probabilistic reasoning based on Bayes' Theorem.
* **Multiclass SVM (Mood Categorization):** Utilizes a One-vs-Rest (OvR) strategy to map complex emotion vectors into corresponding flower categories.

---

## 📊 Mathematical Formulation

### Feature Weighting (TF-IDF)
The system utilizes Term Frequency-Inverse Document Frequency (TF-IDF) to prioritize meaningful words over common ones:
$$IDF(t,D) = \ln\left(\frac{1+|D|}{1+DF(t,D)}\right) + 1$$

### Additive Smoothing (Laplace)
To handle zero-frequency problems, additive smoothing is applied when calculating the probability of a word $w$ given a sentiment class $C$:
$$P(w|C) = \frac{Weight(w,C) + 1}{\sum_{w' \in V} Weight(w',C) + |V|}$$

### SVM Optimization
The Linear SVM learns by minimizing the Hinge Loss function via Stochastic Gradient Descent:
$$w \leftarrow w + \eta(y_i X_i - \lambda w)$$

---

## 📈 Evaluation Results
Bloomix was evaluated using a dataset of 1,142 unique entries and validated by 55 survey respondents.

| Model | Task | Accuracy | Key Finding |
| :--- | :--- | :--- | :--- |
| **Naïve Bayes** | Sentiment Prediction | **97.40%** | Zero-error rate in cross-polarity classification. |
| **Linear SVM** | Mood Categorization | **88.74%** | Superior performance in separating "Complex" emotional states. |

---

## 📂 Project Team
* **Joseph Christian Cinco** – Project Leader & Lead Developer (Concept, Frontend/Backend, ML Integration).
* **Faith P. Rojales** – Developer & Research Contributor.

*Developed as a research project at **De La Salle University - Dasmariñas** (2025-2026).*

---

## 📝 Note on Documentation
The structure and technical formatting of this README.md were developed with the assistance of **Gemini**, an AI collaborator, to ensure alignment with professional documentation standards and technical accuracy.
