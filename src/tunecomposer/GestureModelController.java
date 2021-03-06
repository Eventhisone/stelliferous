package tunecomposer;

import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

/**
 * The controller for gestures (groups of notes).
 * @author Tyler Maule
 * @author Jingyuan Wang
 * @author Kaylin Jarriel
 * @author Zach Turner
 */
public class GestureModelController {
    
    //makes available gestureRectPane, which stores gesture outline
    @FXML Pane gestureRectPane;
    
    //creates a list to store all gesture/grouped notes
    protected ArrayList<ArrayList<NoteRectangle>> gestureNoteGroups;        
    
    //store coordinates for gesture rectangles
    private double gestureMinX;
    private double gestureMinY;
    private double gestureMaxX;
    private double gestureMaxY;
 
    /**
     * Creates space for a group of notes (a gesture).
     */
    public GestureModelController() {
        this.gestureNoteGroups = new ArrayList<>();
    }
    
    /**
     * Removes all gesture rectangles from the gesture pane.
     */
    protected void removeEverything() {
        gestureRectPane.getChildren().clear();
    }
    
    /**
     * Calculates the border of the rectangle which indicates a gesture.
     * @param gesture the gesture for which the rectangle is being calculated
     * @return  coordinates of the rectangle stored in an array
     */
    private ArrayList<Double> calculateBorder(ArrayList<NoteRectangle> gesture) {
       
        NoteRectangle currentRect = gesture.get(0);
        generateCoordinates(currentRect);
       
        determineGestureCoords(gesture);
        
        //calculates and returns the proper coordinates
        ArrayList<Double> borderCords = new ArrayList<>();
        borderCords.add(gestureMinX - Constants.GESTURERECTPADDING);
        borderCords.add(gestureMinY - Constants.GESTURERECTPADDING);
        borderCords.add(gestureMaxX - gestureMinX + 2*Constants.GESTURERECTPADDING);
        borderCords.add(gestureMaxY - gestureMinY + 2*Constants.GESTURERECTPADDING);
        return borderCords;
    }

    /**
     * Generates coordinates for comparison based on a rectangle in the gesture.
     * @param currentRect the rectangle whose coordinates are being generated
     */
    private void generateCoordinates(NoteRectangle currentRect) {
        gestureMinX = currentRect.getX();
        gestureMinY = currentRect.getY();
        gestureMaxX = currentRect.getX() + currentRect.getWidth();
        gestureMaxY = currentRect.getY() + Constants.HEIGHTRECTANGLE;
    }
    
    /**
     * Compares coordinates of all notes in a gesture to determine the maximum 
     * and minimum X and Y values for the gesture rectangle.
     * @param gesture the gestures whose coordinates are being determined
     */
    private void determineGestureCoords(ArrayList<NoteRectangle> gesture) {
        NoteRectangle currentRect;
        for (int i = 1; i < gesture.size(); i++){
            currentRect = gesture.get(i);
            if (gestureMinY > currentRect.getY() ){
                gestureMinY = currentRect.getY() ;
            }
            if (gestureMinX > currentRect.getX()){
                gestureMinX = currentRect.getX();
            }
            if (gestureMaxX < currentRect.getX() + currentRect.getWidth()){
                gestureMaxX = currentRect.getX() + currentRect.getWidth();
            }
            if (gestureMaxY < currentRect.getY()  + Constants.HEIGHTRECTANGLE){
                gestureMaxY = currentRect.getY() + Constants.HEIGHTRECTANGLE ;
            }
        }
    }
    
    /**
     * Updates the gesture rectangle by creating a new one according to the new
     * coordinates.
     * @param gesture the gesture whose rectangle is to be updated
     */
    protected void updateGestureRectangle(ArrayList<NoteRectangle> gesture, String color){       
        //uses coordinates to create and style gesture rectangle
        ArrayList<Double> borderCords = calculateBorder(gesture);        
        Rectangle gestRect = new Rectangle(borderCords.get(0),borderCords.get(1),borderCords.get(2),borderCords.get(3));
        if (color.equals("red")) {
            gestRect.getStyleClass().add("selectedGesture");
        } else {
            gestRect.getStyleClass().add("unselectedGesture");
        }
        gestureRectPane.getChildren().add(gestRect);
    }
    
    /**
     * Upon gesture selection, selects notes within that gesture.
     * @param selectedGesture the gesture whose notes are to be selected
     */
    protected void gestureNoteSelection(ArrayList<NoteRectangle> selectedGesture){
        //clears all gesture rectangles
        gestureRectPane.getChildren().clear();
        
        //checks which notes were selected before creation of selection rectangle 
        //so they remain selected if control is held down regardless of if 
        //they're in the selection rectangle
        ArrayList<NoteRectangle> copySelected = new ArrayList();
        selectedGesture.forEach((e1)->{
            copySelected.add(e1);
        });
        
        for (int j=0 ;j < gestureNoteGroups.size();j++) {
            ArrayList<NoteRectangle> currentGesture = gestureNoteGroups.get(j);
            boolean match = true;
            for (int i=0;i<currentGesture.size();i++) {
                if (!copySelected.contains(currentGesture.get(i))) {
                    match = false;
                    break;
                }
                if (i == currentGesture.size()-1) {
                    currentGesture.forEach((e1)-> {
                        copySelected.remove(e1);
                    });
                }
            }
            if (match) {
                updateGestureRectangle(currentGesture,"red");
            } else {
                updateGestureRectangle(currentGesture,"black");
            }  
        }
    }
    
    /**
     * Checks to see if selected notes are in any gestures. Returns new list of 
     * selected notes if a gesture contains the selected note.
     * @param r the selected notes
     * @param selectNotes the array of selected notes
     * @return the new list of selected notes
     */
    protected ArrayList<NoteRectangle> checkForSelectedNotes(NoteRectangle r, ArrayList<NoteRectangle> selectNotes) {
        for (int i=0 ;i < gestureNoteGroups.size();i++) {
            ArrayList currentGesture = gestureNoteGroups.get(i);
            if (currentGesture.contains(r)) {
                //if selected notes are in gestures, update gestures
                //and take note of other notes in those gestures
                selectNotes = currentGesture;
                break;
            }
        }
        return selectNotes;
    }
    
    /**
     * If a deselected note in in a gesture, deselects that entire gesture.
     * @param rect the note being deselected
     * @param selectNotes the list of selected notes
     * @return the updated list of selected notes
     */
    protected ArrayList<NoteRectangle> checkForDeselectedNotes(NoteRectangle rect, ArrayList<NoteRectangle> selectNotes) {
        for (int i=0 ;i < gestureNoteGroups.size();i++) {
            ArrayList currentGesture = gestureNoteGroups.get(i);
            if (currentGesture.contains(rect)) {
                for(int u=0; u < currentGesture.size();u++){
                    NoteRectangle rectInGesture = (NoteRectangle) currentGesture.get(u);
                    rectInGesture.clearStroke();
                    rectInGesture.notes.getStyleClass().add("unselectedRect"); 
                    if(selectNotes.contains(rectInGesture)) selectNotes.remove(rectInGesture);
                }
                break;
            }
        }
        return selectNotes;
    }
}
