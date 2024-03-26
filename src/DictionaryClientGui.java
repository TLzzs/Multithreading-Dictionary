import GUI.ToggleButton;
import Mapper.Mapper;

import javax.swing.*;
import java.awt.*;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.List;

import static Config.CommunicateConfig.END_OF_LINE;
import static Config.CommunicateConfig.WORD_NOT_FOUND_FROM_SERVER;

public class DictionaryClientGui {
    private CardLayout cardLayout;
    private JPanel cardPanel;

    private final Logger logger;
    private final RequestSendingHandler requestSendingHandler;

    public DictionaryClientGui(Logger logger, RequestSendingHandler requestSendingHandler) {
        this.logger = logger;
        this.requestSendingHandler = requestSendingHandler;
        initializeUI();
    }

    private void initializeUI() {
        JFrame frame = new JFrame("Best Dictionary");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(700, 400);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 0));
        frame.add(mainPanel);

        JPanel sidebar = new JPanel(new GridLayout(4, 1, 0, 10));
        sidebar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        ToggleButton queryButton = new ToggleButton("Query");
        ToggleButton addButton = new ToggleButton("Add");
        ToggleButton removeButton = new ToggleButton("Remove");
        ToggleButton updateButton = new ToggleButton("Update");

        sidebar.add(queryButton);
        sidebar.add(addButton);
        sidebar.add(removeButton);
        sidebar.add(updateButton);

        mainPanel.add(sidebar, BorderLayout.WEST);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        JPanel welcomePanel = createWelcomeCard();
        cardPanel.add(welcomePanel, "Welcome");

        cardPanel.add(createQueryCard(), "Query");
        cardPanel.add(createAddCard(), "Add");
        cardPanel.add(createRemoveCard(), "Remove");
        cardPanel.add(createUpdateCard(), "Update");

        mainPanel.add(cardPanel, BorderLayout.CENTER);

        setupCardSwitching(queryButton, addButton, removeButton, updateButton);

        frame.setVisible(true);
    }

    private JPanel createQueryCard() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Search for a Word"));

        JTextField wordInput = new JTextField();
        wordInput.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Box inputBox = Box.createHorizontalBox();
        inputBox.add(wordInput);
        inputPanel.add(inputBox, BorderLayout.CENTER);

        JButton queryButton = new JButton("Search");
        inputPanel.add(queryButton, BorderLayout.EAST);

        card.add(inputPanel, BorderLayout.NORTH);

        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Outputs"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        card.add(scrollPane, BorderLayout.CENTER);

        queryButton.addActionListener(e -> {
            String word = wordInput.getText().trim();
            if (!word.isEmpty()) {
                new Thread( ()->{
                    String definition = requestSendingHandler.queryServer(word);
                    if (definition.equals(WORD_NOT_FOUND_FROM_SERVER + END_OF_LINE)) {
                        SwingUtilities.invokeLater(() -> resultArea.setText(WORD_NOT_FOUND_FROM_SERVER));
                    } else {
                        definition = Mapper.convertQueryResultToResultArea(definition);
                        String finalDefinition = definition;
                        SwingUtilities.invokeLater(() -> resultArea.setText(finalDefinition));
                    }
                }).start();
            } else {
                resultArea.setText("Please enter a word to query.");
            }
        });

        return card;
    }

    private JPanel createAddCard() {
        JPanel addCard = new JPanel();
        addCard.setLayout(new BoxLayout(addCard, BoxLayout.Y_AXIS));
        addCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel wordPanel = new JPanel();
        wordPanel.setLayout(new BoxLayout(wordPanel, BoxLayout.Y_AXIS));
        wordPanel.setBorder(BorderFactory.createTitledBorder("Word To Add"));

        JTextField wordInput = new JTextField();
        wordInput.setAlignmentX(Component.CENTER_ALIGNMENT);
        wordInput.setMaximumSize(new Dimension(Integer.MAX_VALUE, wordInput.getPreferredSize().height));
        wordPanel.add(wordInput);

        addCard.add(wordPanel);
        addCard.add(Box.createVerticalStrut(10));

        JPanel meaningsPanel = new JPanel(new BorderLayout());
        meaningsPanel.setBorder(BorderFactory.createTitledBorder("Meanings"));

        JPanel meaningsListPanel = new JPanel();
        meaningsListPanel.setLayout(new BoxLayout(meaningsListPanel, BoxLayout.Y_AXIS));
        meaningsPanel.add(new JScrollPane(meaningsListPanel), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearButton = new JButton("Clear");
        JButton addMeaningButton = new JButton("+ Add Meaning");
        JButton submitButton = new JButton("Submit");
        bottomPanel.add(clearButton);
        bottomPanel.add(addMeaningButton);
        bottomPanel.add(submitButton);
        meaningsPanel.add(bottomPanel, BorderLayout.SOUTH);

        List<JTextField> meaningFields = new ArrayList<>();

        addMeaningButton.addActionListener(e -> {
            JTextField newMeaningField = new JTextField();
            JButton removeMeaningButton = new JButton("-");

            Dimension buttonDimension = removeMeaningButton.getPreferredSize();
            newMeaningField.setPreferredSize(new Dimension(newMeaningField.getPreferredSize().width, buttonDimension.height));
            newMeaningField.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonDimension.height));

            removeMeaningButton.addActionListener(ev -> {
                meaningsListPanel.remove(newMeaningField.getParent());
                meaningFields.remove(newMeaningField);
                meaningsListPanel.revalidate();
                meaningsListPanel.repaint();
            });

            JPanel meaningFieldPanel = new JPanel();
            meaningFieldPanel.setLayout(new BoxLayout(meaningFieldPanel, BoxLayout.X_AXIS));
            meaningFieldPanel.add(newMeaningField);
            meaningFieldPanel.add(removeMeaningButton);
            meaningFields.add(newMeaningField);

            meaningsListPanel.add(meaningFieldPanel);
            meaningsListPanel.revalidate();
            meaningsListPanel.repaint();
        });

        addCard.add(meaningsPanel);

        JTextArea statusArea = new JTextArea(2, 20);
        statusArea.setEditable(false);
        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        statusScrollPane.setBorder(BorderFactory.createTitledBorder("Status"));
        addCard.add(statusScrollPane);

        clearButton.addActionListener(e -> {
            meaningFields.clear();
            meaningsListPanel.removeAll();
            meaningsListPanel.revalidate();
            meaningsListPanel.repaint();
            statusArea.setText("");
            wordInput.setText("");
        });

        submitButton.addActionListener(e -> {
            String word = wordInput.getText().trim();
            logger.info("receive word: " + word);
            List<String> meanings = meaningFields.stream()
                                                 .map(JTextField::getText)
                                                 .filter(text -> !text.trim().isEmpty())
                                                 .toList();
            logger.info("receive meaning: " + meanings);

            if (!word.isEmpty() && !meanings.isEmpty()) {
                new Thread(() -> {
                    String response = requestSendingHandler.sendAddToServer(word, meanings);
                    SwingUtilities.invokeLater(() -> {
                        statusArea.setText(response);
                    });
                }).start();
            } else {
                statusArea.setText("Please enter a word and at least one meaning before submitting.");
            }
        });

        return addCard;
    }


    private JPanel createRemoveCard() {
        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Delete A Word And Definitions"));

        JTextField wordInput = new JTextField();
        wordInput.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Box inputBox = Box.createHorizontalBox();
        inputBox.add(wordInput);
        inputPanel.add(inputBox, BorderLayout.CENTER);

        JButton queryButton = new JButton("Delete");
        inputPanel.add(queryButton, BorderLayout.EAST);

        card.add(inputPanel, BorderLayout.NORTH);

        JTextArea resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Output"),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JScrollPane scrollPane = new JScrollPane(resultArea);
        card.add(scrollPane, BorderLayout.CENTER);

        queryButton.addActionListener(e -> {
            String word = wordInput.getText().trim();
            if (!word.isEmpty()) {
                new Thread (()-> {
                    String status = requestSendingHandler.removeFromServer(word);
                    SwingUtilities.invokeLater(() -> {
                        resultArea.setText(status);
                    });
                }).start();
            } else {
                resultArea.setText("Please enter a word to query.");
            }
        });

        return card;
    }

    private JPanel createUpdateCard() {
        JPanel updateCard = new JPanel();
        updateCard.setLayout(new BoxLayout(updateCard, BoxLayout.Y_AXIS));
        updateCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel wordPanel = new JPanel(new BorderLayout());
        wordPanel.setBorder(BorderFactory.createTitledBorder("Word To Update"));
        wordPanel.setMaximumSize(new Dimension(600, 60));

        Box wordInputBox = Box.createHorizontalBox();
        JTextField wordInput = new JTextField(20);
        wordInputBox.add(wordInput);
        wordPanel.add(wordInputBox, BorderLayout.CENTER);

        JButton fetchDefinitionButton = new JButton("Fetch Definition");
        JButton clearButton = new JButton("Clear");  // Clear button next to Fetch Definition button

        // Panel to hold both Fetch and Clear buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        buttonPanel.add(fetchDefinitionButton);
        buttonPanel.add(clearButton);
        wordPanel.add(buttonPanel, BorderLayout.EAST);

        updateCard.add(wordPanel);
        updateCard.add(Box.createVerticalStrut(10));


        JPanel meaningsPanel = new JPanel(new BorderLayout());
        meaningsPanel.setBorder(BorderFactory.createTitledBorder("Edit Meanings"));

        JPanel meaningsListPanel = new JPanel();
        meaningsListPanel.setLayout(new BoxLayout(meaningsListPanel, BoxLayout.Y_AXIS));
        JScrollPane meaningsScrollPane = new JScrollPane(meaningsListPanel);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addMeaningButton = new JButton("+ Add Meaning");
        addMeaningButton.setVisible(false);
        JButton submitButton = new JButton("Submit");
        submitButton.setVisible(false);
        bottomPanel.add(addMeaningButton);
        bottomPanel.add(submitButton);

        meaningsPanel.add(meaningsScrollPane, BorderLayout.CENTER);
        meaningsPanel.add(bottomPanel, BorderLayout.SOUTH);

        updateCard.add(meaningsPanel);

        JTextArea statusArea = new JTextArea(2, 20);
        statusArea.setEditable(false);
        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        statusScrollPane.setBorder(BorderFactory.createTitledBorder("Status"));
        updateCard.add(statusScrollPane);

        List<JTextField> meaningFields = new ArrayList<>();

        fetchDefinitionButton.addActionListener(e -> {
            String word = wordInput.getText().trim();
            meaningsListPanel.removeAll();
            meaningFields.clear();
            if (!word.trim().isEmpty()) {
                List<String> definitions = requestSendingHandler.fetchDefinitions(word);
                if (!definitions.get(0).equals(WORD_NOT_FOUND_FROM_SERVER)) {
                    for (String def : definitions) {
                        addMeaningField(meaningsListPanel, meaningFields, def);
                    }
                    wordInput.setEditable(false);
                    addMeaningButton.setVisible(true);
                    submitButton.setVisible(true);
                    statusArea.setText("");
                } else {
                    meaningsListPanel.removeAll();
                    meaningFields.clear();
                    meaningsListPanel.revalidate();
                    meaningsListPanel.repaint();
                    statusArea.setText("Word Not Found Please Try Another One");
                }
            } else {
                statusArea.setText("Input is empty, please give an input");
            }

        });

        clearButton.addActionListener(e -> {
            wordInput.setEditable(true);
            addMeaningButton.setVisible(false);
            submitButton.setVisible(false);
            wordInput.setText("");
            meaningsListPanel.removeAll();
            meaningFields.clear();
            statusArea.setText("");
            meaningsListPanel.revalidate();
            meaningsListPanel.repaint();
        });


        // Add meaning button action
        addMeaningButton.addActionListener(e -> {
            addMeaningField(meaningsListPanel, meaningFields, "");
        });

        // Submit button action
        submitButton.addActionListener(e -> {
            String word = wordInput.getText().trim();
            List<String> meanings = meaningFields.stream()
                    .map(JTextField::getText)
                    .filter(text -> !text.trim().isEmpty())
                    .toList();
            if ( !word.isEmpty() &&  !meanings.isEmpty()) {
                new Thread (()-> {
                    String status = requestSendingHandler.updateDictionary(word, meanings);
                    SwingUtilities.invokeLater(() -> {
                        statusArea.setText(status);
                    });
                }).start();
            } else {
                statusArea.setText("Please Enter A Word To Fetch Current Definition");
            }
        });
        return updateCard;
    }

    private void addMeaningField(JPanel meaningsListPanel, List<JTextField> meaningFields, String text) {
        JTextField newMeaningField = new JTextField(text);
        JButton removeMeaningButton = new JButton("-");

        Dimension buttonDimension = removeMeaningButton.getPreferredSize();
        newMeaningField.setPreferredSize(new Dimension(newMeaningField.getPreferredSize().width, buttonDimension.height));
        newMeaningField.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonDimension.height));

        removeMeaningButton.addActionListener(e -> {
            meaningsListPanel.remove(newMeaningField.getParent());
            meaningFields.remove(newMeaningField);
            meaningsListPanel.revalidate();
            meaningsListPanel.repaint();
        });

        JPanel meaningFieldPanel = new JPanel();
        meaningFieldPanel.setLayout(new BoxLayout(meaningFieldPanel, BoxLayout.X_AXIS));
        meaningFieldPanel.add(newMeaningField);
        meaningFieldPanel.add(removeMeaningButton);
        meaningFields.add(newMeaningField);

        meaningsListPanel.add(meaningFieldPanel);
        meaningsListPanel.revalidate();
        meaningsListPanel.repaint();

    }




    private JPanel createWelcomeCard() {
        JPanel welcomePanel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Welcome to the Best Dictionary", JLabel.CENTER);
        welcomePanel.add(welcomeLabel);
        return welcomePanel;
    }

    private void setupCardSwitching(ToggleButton queryButton, ToggleButton addButton,
                                    ToggleButton removeButton, ToggleButton updateButton) {
        queryButton.addActionListener(e -> {
            queryButton.setSelected(true);
            addButton.setSelected(false);
            removeButton.setSelected(false);
            updateButton.setSelected(false);
            cardLayout.show(cardPanel, "Query");
        });
        addButton.addActionListener(e -> {
            queryButton.setSelected(false);
            addButton.setSelected(true);
            removeButton.setSelected(false);
            updateButton.setSelected(false);
            cardLayout.show(cardPanel, "Add");
        });
        removeButton.addActionListener(e -> {
            queryButton.setSelected(false);
            addButton.setSelected(false);
            removeButton.setSelected(true);
            updateButton.setSelected(false);
            cardLayout.show(cardPanel, "Remove");
        });
        updateButton.addActionListener(e -> {
            queryButton.setSelected(false);
            addButton.setSelected(false);
            removeButton.setSelected(false);
            updateButton.setSelected(true);
            cardLayout.show(cardPanel, "Update");
        });
    }
}
