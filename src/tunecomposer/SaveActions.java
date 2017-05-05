package tunecomposer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * A class to execute saving Compositions to, and reading them from, a .txt file
 * @author tmaule
 */
public class SaveActions {
    
    private final MainController mainController;
    protected String fileOperatedOn;
    
    /**
     * Constructor that connects a SaveActions object to a given mainController
     * @param givenMainController 
     */
    protected SaveActions(MainController givenMainController){
        this.mainController = givenMainController;
    }
     
    /**
     * Allows the user to select a txt file from which to copy notes into
     * their composition.
     * @return a string describing the notes
     * @throws FileNotFoundException 
     */
    protected String readFile() throws FileNotFoundException{
        String noteString = "";
        Stage fileStage = new Stage();
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("TXT", "*.txt"));
        fileChooser.setTitle("Open Resource File");
        File selectedFile = fileChooser.showOpenDialog(fileStage);
        fileStage.show();
        if (selectedFile != null) {
            Scanner scanner = new Scanner(selectedFile);
            while (scanner.hasNext()){
                noteString += scanner.next();
            }
            fileOperatedOn = selectedFile.toString();
        }
        fileStage.close();
        
        //alert composition that this file is being worked off of
        
        return noteString;
    }
    
    /**
     * Creates a txt file to which it copies the composition's notes.
     * @param filename name of file to be written to
     * @throws IOException 
     */
    protected void copyCompositionToFile(String filename) throws IOException{
         if (!filename.isEmpty()){
            FileWriter fstream = new FileWriter(filename);
            try (BufferedWriter out = new BufferedWriter(fstream)) {
                out.flush();
                fstream.flush();
                out.write(mainController.compositionFileInteractions.notesToString(mainController.getRectList(),mainController.gestureModelController.gestureNoteGroups,false));
                fileOperatedOn = (filename + ".txt");
                mainController.setOperatingOnFile(filename);
                mainController.setIsSaved(Boolean.TRUE);
                
            }
        } 
    }
    
    /**
     * Presents dialogs that allow a user to choose a name when saving
     * a Composition as a .txt file
     * @throws IOException 
     */
    protected void chooseFileName() throws IOException{        
        TextInputDialog dialog = new TextInputDialog("Choose File Name");

        dialog.setTitle("File >> Save As");
        dialog.setHeaderText("Save As");
        dialog.setContentText("Please enter a valid file name:");
        
        Optional<String> result = dialog.showAndWait();
                
        if (result.isPresent() && isValidFileName(result.get())){
            copyCompositionToFile(result.get()+".txt");
        }  else if (result.isPresent() && !isValidFileName(result.get())){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error Dialog");
            alert.setHeaderText("Invalid File Name");
            alert.setContentText("Do not include periods, slashes or the null character in file names.");

            alert.showAndWait();
            chooseFileName();
        }      
    }
    
    /**
     * Determines whether a given file name is valid
     * @param filename
     * @return boolean describing whether or not a file name is valid
     */
    private Boolean isValidFileName(String filename){
        return !(filename.isEmpty() || filename.contains("\0") || filename.contains(".") || filename.contains("/"));
    }
    
    /**
     * Allows the user to write/copy selected notes to a txt file in the proper
     * syntax.
     * @param noteString a string representing the current composition
     * @param file a file to save the string to 
     */
    protected void saveFile(String noteString, File file){
        try {
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(noteString);
            fileWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(MenuBarController.class.getName()).log(Level.SEVERE, null, ex);
        }
         
    }
    
    /**
     * Reads a file, and translates the string inside into a Composition.
     * @throws FileNotFoundException 
     */
    protected void openFile() throws FileNotFoundException{
        String noteString = readFile();
        if (!noteString.isEmpty()){
            mainController.compositionFileInteractions.notesFromString(noteString,0,0);
            mainController.setIsSaved(Boolean.TRUE);
        }
    }
    
    /**
     * Creates a new composition.
     * @param e an ActionEvent
     */
    protected void newComposition(ActionEvent e){
        mainController.restart();
        mainController.setOperatingOnFile("");
        mainController.menuBarController.checkButtons(); 
    }
    
    /**
     * Presents user with an alert dialog and options to save the composition
     * if they try to create a new composition without saving
     * @param e an ActionEvent
     * @throws IOException 
     */
    protected void invokeNewWithoutSaving(ActionEvent e) throws IOException{
        Alert confirmationWindow = new Alert(Alert.AlertType.CONFIRMATION,"Are you sure you create a new composition without saving?");
        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No (save)");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        confirmationWindow.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo,buttonTypeCancel);

        Optional<ButtonType> result = confirmationWindow.showAndWait();
        if (result.get() == buttonTypeYes){
            mainController.restart();
            mainController.menuBarController.checkButtons();
        } else if (result.get() == buttonTypeNo) {
            mainController.menuBarController.handleSaveAction(e);
            mainController.restart();
        } else {
            confirmationWindow.hide();
        }
    }
    
    protected void invokeExitWithoutSaving(ActionEvent e) throws IOException{
        Alert confirmationWindow = new Alert(AlertType.CONFIRMATION,"Are you sure you want to quit without saving?");
        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No (save)");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        confirmationWindow.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo,buttonTypeCancel);

        Optional<ButtonType> result = confirmationWindow.showAndWait();
        if (result.get() == buttonTypeYes){
            System.exit(0);
        } else if (result.get() == buttonTypeNo) {
            mainController.menuBarController.handleSaveAction(e);
            System.exit(0);
        } else {
            confirmationWindow.hide();
        }
    }
    
    protected void invokeOpenWithoutSaving(ActionEvent e) throws FileNotFoundException, IOException{
        Alert confirmationWindow = new Alert(AlertType.CONFIRMATION,"Are you sure you open a composition without saving?");
        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("Save");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonData.CANCEL_CLOSE);

        confirmationWindow.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo,buttonTypeCancel);

        Optional<ButtonType> result = confirmationWindow.showAndWait();
        if (result.get() == buttonTypeYes){
            mainController.restart();
            mainController.saveActions.openFile();
            mainController.menuBarController.checkButtons();
        } else if (result.get() == buttonTypeNo) {
            mainController.menuBarController.handleSaveAction(e);
            mainController.restart();
            mainController.saveActions.openFile(); 
        } else {
            confirmationWindow.hide();
        }
    }
}
