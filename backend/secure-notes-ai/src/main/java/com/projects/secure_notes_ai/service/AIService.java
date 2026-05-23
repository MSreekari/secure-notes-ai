package com.projects.secure_notes_ai.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AIService {

    private final ChatLanguageModel chatModel;

    public AIService(ChatLanguageModel chatModel) {
        this.chatModel = chatModel;
    }
    // to generate summary
    public String generateSummary(String content){
        if(content==null || content.trim().isEmpty()){
            return "Note is empty summary cannot be generated";
        }
        String prompt = "Summarize the following text in one or two concise sentences. " +
                "Do not include any introductory remarks—return ONLY the summary text:\n\n" + content;
        try{
            return chatModel.generate(prompt).trim();
        }catch(Exception e){
            return "Unable to generate summary";
        }
    }
    // to extract keywords
    public List<String> extractKeywords(String content){
        if(content==null || content.trim().isEmpty()){
            return List.of("empty");
        }
        String prompt = "Extract up to 5 distinct, single-word technical or core keywords from the following text. " +
                "Return them ONLY as a single comma-separated list without spaces or bullets (e.g., java,spring,security):\n\n" +  content;
        try{
            String response = chatModel.generate(prompt);
            return Arrays.stream(response.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(word -> !word.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return List.of("general");
        }
    }
    // check if the note is sensitive
    public boolean isNoteSensitive(String content){
        if(content==null || content.trim().isEmpty()){
            return false;
        }
        String prompt = "\"You are an enterprise security compliance classifier. Analyze the following text for high-risk, sensitive data.\n" +
                "\n" +
                "CRITERIA FOR SENSITIVE DATA:\n" +
                "1. Credentials: Passwords, SSH private keys, IAM secret keys, or unencrypted authentication tokens.\n" +
                "2. API Keys: Clear-text cloud platform tokens (e.g., AWS, Google Cloud, OpenAI, Groq keys starting with 'gsk_').\n" +
                "3. PII (Personally Identifiable Information): Raw credit card numbers, Aadhaar numbers, Social Security Numbers, or bank account details.\n" +
                "4. Confidential Code Configuration: Database connection strings containing passwords or secret cryptographic salts.\n" +
                "\n" +
                "If the text contains ANY of the high-risk items listed above, respond with exactly one word: SENSITIVE\n" +
                "If the text does NOT contain any of these items and is safe for general storage, respond with exactly one word: SAFE\n" +
                "\n" +
                "CRITICAL INSTRUCTION: Return ONLY the single word 'SENSITIVE' or 'SAFE'. Do not include any introductory text, concluding remarks, punctuation, explanations, or formatting.\n" +
                "\n" +
                "TEXT TO EVALUATE:\n" + content;
        try{
            String response = chatModel.generate(prompt).trim().toLowerCase();
            if(response.equals("sensitive")){
                return true;
            }else{
                return false;
            }
        }catch(Exception e){
            return false;
        }
    }
}
