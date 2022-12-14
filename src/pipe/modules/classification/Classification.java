/**
 * Classification Module
 * @author James D Bloom 2003-03-12
 * @author Maxim Gready 2004
 * @author minor changes Will Master 02/2007 
 *
 * This module was severly broken in the 2003 release. It should now be
 * producing correct results but is still being fixed up.
 *  
 *  
 */

package pipe.modules.classification;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.BoxLayout;
import javax.swing.JDialog;

import pipe.common.dataLayer.DataLayer;
import pipe.gui.CreateGui;
import pipe.gui.widgets.ButtonBar;
import pipe.gui.widgets.PetriNetChooserPanel;
import pipe.gui.widgets.ResultsHTMLPane;
import pipe.modules.Module;

/** Classification class implements petri net classification */
public class Classification implements Module {
  private static final String MODULE_NAME = "Classification";

  private PetriNetChooserPanel sourceFilePanel;
  private ResultsHTMLPane results;
  
  
  public void run(DataLayer pnmlData) {
    // Build interface
    JDialog guiDialog = new JDialog(CreateGui.getApp(),MODULE_NAME,true);
    
    // 1 Set layout
    Container contentPane=guiDialog.getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
    
    // 2 Add file browser
    contentPane.add(sourceFilePanel=new PetriNetChooserPanel("Source net",pnmlData));
    
    // 3 Add results pane
    contentPane.add(results=new ResultsHTMLPane(pnmlData.getURI()));
    
    // 4 Add button
    contentPane.add(new ButtonBar("Classify",classifyButtonClick));

    // 5 Make window fit contents' preferred size
    guiDialog.pack();
    
    // 6 Move window to the middle of the screen
    guiDialog.setLocationRelativeTo(null);
    
    guiDialog.setVisible(true);
  }

  public String getName() {
    return MODULE_NAME;
  }
  

  /**
	 * Classify button click handler
	 */
  ActionListener classifyButtonClick=new ActionListener() {
    public void actionPerformed(ActionEvent arg0) {
      DataLayer sourceDataLayer=sourceFilePanel.getDataLayer();
      String s="<h2>Petri net classification results</h2>";
      if(sourceDataLayer==null) return;
      if(!sourceDataLayer.getPetriNetObjects().hasNext()) s+="No Petri net objects defined!";
      else s+=ResultsHTMLPane.makeTable(new String[]{
                "State Machine"           ,""+stateMachine(sourceDataLayer),
                "Marked Graph"            ,""+markedGraph(sourceDataLayer),
                "Free Choice Net"         ,""+freeChoiceNet(sourceDataLayer),
                "Extended Free Choice Net",""+extendedFreeChoiceNet(sourceDataLayer),
                "Simple Net"              ,""+simpleNet(sourceDataLayer),
                "Extended Simple Net"     ,""+extendedSimpleNet(sourceDataLayer)                
              },2,false,true,false,true);
      results.setText(s);
    }
  };
  
  /**
   * State machine detection
   * @return <i>true</i> iff all transitions have at most one input or output
   * SM iff &forall; t &isin; T: |&bull;t| = |t&bull;| &#8804; 1
   * <pre>
   * P - T - P
   * P - T - P   true (one input/output each)
   *
   * P - T - P
   *       \     false (>1 output)
   *         P
   *
   * P - T - P
   *   /         false (>1 input)
   * P
   * </pre>
   * @author Maxim Gready after James D Bloom
   */
  protected boolean stateMachine(DataLayer pnmlData){
//    Transition[] transitions=pnmlData.getTransitions();
//	  Transition[] transitions = getTransitions(pnmlData);
	  int transitionsCount = pnmlData.getTransitionsCount();
	  
	  if (transitionsCount != 0)
      for (int transitionNo=0; transitionNo<transitionsCount; transitionNo++)
        if ((countTransitionInputs(pnmlData,transitionNo)>1)||(countTransitionOutputs(pnmlData,transitionNo)>1))
          return false;
    return true;
  }

  /**
   * Marked graph detection
   * @return true iff all places have at most one input or output
   * MG iff &forall; p &isin; P: |&bull;p| = |t&bull;| &#8804; 1
   * <pre>
   * T - P - T
   * T - P - T   true (one input/output each)
   *
   * T - P - T
   *       \     false (>1 output)
   *         T
   *
   * T - P - T
   *   /         false (>1 input)
   * T
   * </pre>
   * @author Maxim Gready after James D Bloom
   */
  protected boolean markedGraph(DataLayer pnmlData){
//    Place[] places=pnmlData.getPlaces();
//	  Place[] places=getPlaces(pnmlData);

	  int placeCount = pnmlData.getPlacesCount();	  
	  if (placeCount != 0)
      for (int placeNo=0; placeNo<placeCount; placeNo++)
        if ((countPlaceInputs(pnmlData,placeNo)>1)||(countPlaceOutputs(pnmlData,placeNo)>1))
          return false;
    return true;
  }

  /**
   * Free choice net detection
   * @return true iff no places' outputs go to the same transition, unless those places both have only one output
   * FC-net iff &forall; p, p&prime; &isin; P: p &#x2260; p&prime; &#x21d2; (p&bull;&#x2229;p&prime;&bull; = 0 or |p&bull;| = |p&prime;&bull;| &#8804; 1)
   * <pre>
   * P - T
   * P - T       true (no common outputs)
   *   \
   *     T
   *
   * P - T
   *   /         true (common outputs but both have only one output)
   * P
   *
   * P - T
   *   X
   * P - T       false (common outputs, both have >1 output)
   *
   * P - T
   *   /
   * P - T       false (common outputs but one has >1 output)
   * </pre>
   * @author Maxim Gready after James D Bloom
   */
  protected boolean freeChoiceNet(DataLayer pnmlData){
//    Place[] places=pnmlData.getPlaces();
//    Place[] places=getPlaces(pnmlData);

	  int placeCount = pnmlData.getPlacesCount();	  
	  int[] fps1,fps2; // forwards place sets for p and p'
      if (placeCount != 0)
      for (int placeNo=0; placeNo<placeCount; placeNo++)
        for (int placeDashNo=placeNo+1; placeDashNo<placeCount;placeDashNo++) {
          fps1=forwardsPlaceSet(pnmlData,placeNo);
          fps2=forwardsPlaceSet(pnmlData,placeDashNo);
          if (intersectionBetweenSets(fps1,fps2) && ((fps1.length>1) || (fps2.length>1))) {
            return false;
          }
        }
    return true;
  }

  /**
   * Extended free choice net detection
   * @return true iff no places' outputs go to the same transition, unless both places outputs are identical
   * EFC-net iff &forall; p, p&prime; &isin; P: p &#x2260; p&prime; &#x21d2; (p&bull;&#x2229;p&prime;&bull; = 0 or p&bull; = p&prime;&bull;)
   * <pre>
   * P - T
   * P - T       Yes (no common outputs)
   *   \
   *     T
   *
   * P - T
   *   /
   * P - T       No (common outputs, outputs not identical)
   *
   * P - T
   *   X
   * P - T       Yes (common outputs identical) *** only addition to normal free choice net
   *
   * P - T       Yes (common outputs identical)
   *   /
   * P
   * </pre>
   * @author Maxim Gready after James D Bloom
   */
  protected boolean extendedFreeChoiceNet(DataLayer pnmlData){
//    Place[] places=pnmlData.getPlaces();
//	  Place[] places=getPlaces(pnmlData);
	  int placeCount = pnmlData.getPlacesCount();
	  
	  int[] fps1,fps2; // forwards place sets for p and p'
      if (placeCount != 0)
      for (int placeNo=0; placeNo<placeCount; placeNo++)
        for (int placeDashNo=placeNo+1; placeDashNo<placeCount;placeDashNo++) {
          fps1=forwardsPlaceSet(pnmlData,placeNo);
          fps2=forwardsPlaceSet(pnmlData,placeDashNo);
          if (intersectionBetweenSets(fps1,fps2) && !Arrays.equals(fps1,fps2)) {
            return false;
          }
        }
    return true;
  }

  /**
   * Simple net (SPL-net) detection
   * @return true iff no places' outputs go to the same transition, unless one of the places only has one output
   * SPL-net iff &forall; p, p&prime; &isin; P: p &#x2260; p&prime; &#x21d2; (p&bull;&#x2229;p&prime;&bull; = 0 or |p&bull;| &#8804; 1 or |p&prime;&bull;| &#8804; 1)
   * <pre>
   * P - T
   * P - T       true (no common outputs)
   *   \
   *     T
   *
   * P - T
   *   /         true (common outputs, both only have one)
   * P
   *
   * P - T
   *   /         true (common outputs, one place has only one)
   * P - T
   *
   * P - T
   *   X         false (common outputs, neither has only one)
   * P - T
   *
   * P - T
   *   \
   *     T       false (common outputs, neither has only one)
   *   /
   * P - T
   * </pre>
   * @author Maxim Gready after James D Bloom
   */
  protected boolean simpleNet(DataLayer pnmlData){	  
//    Place[] places=pnmlData.getPlaces();
//	  Place[] places=getPlaces(pnmlData);

	  int placeCount = pnmlData.getPlacesCount();	  
	  if (placeCount != 0)
      for (int placeNo=0; placeNo<placeCount; placeNo++)
        for (int placeDashNo=0; placeDashNo<placeCount; placeDashNo++)
          if (placeDashNo!=placeNo) {
            int[] placeSet=forwardsPlaceSet(pnmlData,placeNo);
            int[] placeDashSet=forwardsPlaceSet(pnmlData,placeDashNo);
            if (
                intersectionBetweenSets(placeSet,placeDashSet) &&
                (countPlaceOutputs(pnmlData,placeNo)>1)&&
                (countPlaceOutputs(pnmlData,placeDashNo)>1)
               ) return false;
          }
    return true;
  }

  /**
   * Extended simple net (ESPL-net) detection
   * @return true iff no places' outputs go to the same transition, unless one of the places' outputs is a subset of or equal to the other's
   * ESPL-net iff &forall; p, p&prime; &isin; P: p &#x2260; p&prime; &#x21d2; (p&bull;&#x2229;p&prime;&bull; = 0 or p&bull; &#x2286; p&prime;&bull; or p&prime;&bull; &#x2286; p&bull;)
   * <pre>
   * P - T
   * P - T       Yes (no common outputs)
   *   \
   *     T
   *
   * P - T
   *   /
   * P - T       Yes (common outputs, first place's outputs is subset of second)
   *
   * P - T
   *   X
   * P - T       Yes (common outputs, identical)
   *
   * P - T       Yes (common outputs, identical)
   *   /
   * P
   *
   * P - T
   *   \
   *     T       false (common outputs, neither is subset of other)
   *   /
   * P - T
   *
   *     T
   *   /
   * P - T       true (common outputs, second's is subset of first's)
   *   X
   * P - T
   * </pre>
   * @author Maxim Gready after James D Bloom
   */
  protected boolean extendedSimpleNet(DataLayer pnmlData){
//    Place[] places=pnmlData.getPlaces();
//	  Place[] places=getPlaces(pnmlData);
	  
	  int placeCount = pnmlData.getPlacesCount();
	  if (placeCount != 0)
      for (int placeNo=0; placeNo<placeCount; placeNo++)
        for (int placeDashNo=0; placeDashNo<placeCount; placeDashNo++)
          if(placeDashNo != placeNo) {
            int[] placeSet = forwardsPlaceSet(pnmlData,placeNo);
            int[] placeDashSet = forwardsPlaceSet(pnmlData,placeDashNo);
            if (
                intersectionBetweenSets(placeSet,placeDashSet) &&
                (!isSubset(placeSet,placeDashSet))
               )
              return false;
          }
    return true;
  }  


  /**
   * Counts inputs to given place
	 * @param PlaceNo
	 * @return number of arcs leading to the given place number; -1 on error
	 */
  private int countPlaceInputs(DataLayer pnmlData,int PlaceNo) {
    int[][] backwards=pnmlData.getForwardsIncidenceMatrix();
    // The forwards incidence matrix is like this:
    // | 0 1 |     P0 ->- T0 ->- P1
    // | 1 0 |       `-<- T1 -<-'
    // where rows are place numbers and columns are transition numbers.
    // The number is the weight of the arc from the corresponding transition
    // to the corresponding place.
    // It can also be considered the backwards incidence matrix for the place.
    int count=0;

    if (PlaceNo<backwards.length) {
      for (int TransitionNo=0; TransitionNo<backwards[PlaceNo].length; TransitionNo++)
        if (backwards[PlaceNo][TransitionNo]!=0) count++;
    } else return -1;
    return count;
  }

  // Counts outputs from given place
  // @return number of arcs leading from the given place number; -1 on error
  private int countPlaceOutputs(DataLayer pnmlData,int PlaceNo) {
    int[][] forwards = pnmlData.getBackwardsIncidenceMatrix(); // transition backwards = place forwards
    int count=0;

    if (PlaceNo>=forwards.length) return -1;

    for (int TransitionNo=0; TransitionNo<forwards[PlaceNo].length; TransitionNo++)
      if (forwards[PlaceNo][TransitionNo]!=0) count++;

    return count;
  }

  // Counts inputs to given transition
  // @return number of arcs leading to the given transition number; -1 on error
  private int countTransitionInputs(DataLayer pnmlData,int TransitionNo) {
    int[][] backwards=pnmlData.getBackwardsIncidenceMatrix();
    int count=0;

    if ((backwards.length<1) || (TransitionNo>=backwards[0].length)) return -1;

    for (int PlaceNo=0; PlaceNo<backwards.length; PlaceNo++)
      if (backwards[PlaceNo][TransitionNo] != 0) count++;

    return count;
  }

  /**
   * Counts outputs from given transition
   * @return number of arcs leading from the given transition number; -1 on error
   */
  private int countTransitionOutputs(DataLayer pnmlData,int TransitionNo) {
    int[][] forwards=pnmlData.getForwardsIncidenceMatrix();
    int count=0;

    if ((forwards.length<1) || (TransitionNo>=forwards[0].length)) return -1;

    for (int PlaceNo=0; PlaceNo<forwards.length; PlaceNo++)
      if (forwards[PlaceNo][TransitionNo]!=0) count++;

    return count;
  }

  // returns array of ints set to indices of transitions output to by given place
  private int[] forwardsPlaceSet(DataLayer pnmlData,int PlaceNo){
    int[][] forwards = pnmlData.getBackwardsIncidenceMatrix();
    ArrayList postsetArrayList = new ArrayList();

    for(int TransitionNo = 0 ; TransitionNo < forwards[PlaceNo].length ; TransitionNo++) {
      if(forwards[PlaceNo][TransitionNo] != 0) {
        postsetArrayList.add(new Integer(TransitionNo));
      }
    }

    int[] postset = new int[postsetArrayList.size()];

    for(int postsetPosition = 0 ; postsetPosition < postset.length ; postsetPosition++) {
      postset[postsetPosition] = ((Integer)postsetArrayList.get(postsetPosition)).intValue();
    }

    return postset;
  }

  protected boolean intersectionBetweenSets(int[] setOne, int[] setTwo) {
    for(int onePosition = 0 ; onePosition < setOne.length ; onePosition++) {
      for(int twoPosition = 0 ; twoPosition < setTwo.length ; twoPosition++) {
        if(setOne[onePosition] == setTwo[twoPosition]) {
          return true;
        }
      }
    }
    return false;
  }

  // returns true if either array is a subset of the other
  private static boolean isSubset(int[] A, int[] B){

    boolean isSubsetOf = false;

    // first you must eliminate all dublicate values from the sets, so run through each set
    // and replace all dublicates with an inpossible (flag) value, e.g. -1
    int[] AA;
    AA = reduce(A);
    int[] BB;
    BB = reduce(B);

    // then you must determine which set has the smallest cardinality (number of
    // distinct values, this is why we did the above operation)
    int AAcard = cardinality(AA);
    int BBcard = cardinality(BB);

    // then look for the set with smaller cardinality being subset of the set with greater cardinality

    // make sure 1st agument has smaller cardinality than 2nd
    if (AAcard< BBcard)
      isSubsetOf = findSubset(AA, BB);
    else
      isSubsetOf = findSubset(BB, AA);

    return isSubsetOf;
  }

  // eliminate all duplicate values from the set, so run through the set
  // and replace all duplicates with an inpossible (flag) value, e.g. -1
  private static int[] reduce(int[] X){

    for (int i=0; i < X.length; i++){
      for (int j = 0; j < i; j++){ // check values before current index
        if (X[i] == X[j]) {// value already exists in the set
          X[i] = -1; // flag dublicate
          break;  // and break the inner loop
        }
      }
    }

    return X;
  }

  // determine set cardinality (number of distinct values)
  private static int cardinality(int[] X){
    int c = 0; // cardinality
    for (int i = 0; i < X.length; i++)
      if (X[i] != -1)
        c++;

    return c;
  }

  // A has smaller cardinality than B, so only A can be subset of B and not the opposite
  private static boolean findSubset(int[] A, int[] B){
    boolean isSubset = true;
    boolean elementExists;

    // loop through A and see if each of its elements is contained in B
    // if at least one is not contained, then it is not a subset. watch for flags -1
    for (int i = 0; i < A.length; i++){
      elementExists = false;
      for (int j = 0; j < B.length; j++){
        if (A[i] == B[j] && A[i] != -1){ // ith element of A exists in B
          elementExists = true;
          break;
        }
      }
      // test for particular ith element of A in all of B
      if (!elementExists)
        return false;
    }

    return isSubset;
  }
}
