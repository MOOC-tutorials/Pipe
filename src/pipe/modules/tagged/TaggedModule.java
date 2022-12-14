package pipe.modules.tagged;



/**
 * Tagged Net -> .mod Converter module.
 * @author Nick Dingle (after Barry Kearns) 
 */


import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

import pipe.common.dataLayer.DataLayer;
import pipe.common.dataLayer.StateGroup;
import pipe.gui.CreateGui;
import pipe.gui.widgets.ButtonBar;
import pipe.modules.Module;


public class TaggedModule implements Module
{
	
	private static final String MODULE_NAME = "Tagged Net Converter";
	
	private DataLayer pnmlData;		// Petri Net to be analysed
	private StateGroup[] stateGroupData;
	
  
	  public TaggedModule() {}
	
	  /**  The module name  */
	  public String getName()
	  {
	    return MODULE_NAME;
	  }

 
  
  // Main GUI elements
  private JDialog guiDialog;
  private JTabbedPane tabbedPane;

  private ProgressBarHTMLPane progressPane = null;
  private JPanel resultsPanel = null;
  private ServerPanelProcessors serverPanel = null;
  
 
  
  // State lists panel
  JList startStatesList, endStatesList;
  JButton addBtn, removeBtn, editBtn;
  
  // Time settings panel
  JTextField startTime, endTime;
  JSpinner timeStep;
  JComboBox methodCombo;

  public void run (DataLayer pnmlDataIn)
  {
  
  	pnmlData = pnmlDataIn;
  	stateGroupData = pnmlDataIn.getStateGroups();
  
    
  	// Create tabbed pane
  	tabbedPane = new JTabbedPane();
  	
  	// Build primary tab
  	guiDialog = new JDialog(CreateGui.getApp(),MODULE_NAME,true);
    Container contentPane=guiDialog.getContentPane();
    contentPane.setLayout(new BoxLayout(contentPane,BoxLayout.PAGE_AXIS));
    
    // 1 Set layout
  	JPanel setupPanel = new JPanel();
    setupPanel.setLayout(new BoxLayout(setupPanel,BoxLayout.PAGE_AXIS));


    // 2 Add Server selection panel 	
 //   serverPanel = new ServerPanelProcessors(guiDialog);
 //   setupPanel.add(serverPanel.getPanel() );

    
    // 3 Add Source - Destination States selection panel
    setupPanel.add( getStatesPanel() );   
    
    // 4 Add Passage time parameters
    setupPanel.add( getAnalysisPanel() );

    // 5 Add Convert button
    setupPanel.add(new ButtonBar("Convert",convertButtonClick));
    
    
    // 6 Add setup panel to tabbed pane, add tabbed pane to guiDialog
    tabbedPane.addTab("Setup", setupPanel);    
    contentPane.add(tabbedPane);
    
    
    // 7 Make window fit contents' preferred size, centre on screen, display
    guiDialog.pack();
    guiDialog.setLocationRelativeTo(null);    
    guiDialog.setVisible(true);

  }



  ActionListener convertButtonClick = new ActionListener() {
	    public void actionPerformed(ActionEvent arg0)
	    {
	    	// Returns the currently selected server (-1 if none selected)
//	    	int selectedServer = serverPanel.getSelectedServerIndex();  	
	    		
	    	// Returns currently selected source and destination states
	    	ArrayList sourceStateGroups = getSelectedStateGroups(startStatesList);
	    	ArrayList destinationStateGroups = getSelectedStateGroups(endStatesList);
	    	
	    	AnalysisSetting analysisSettings = getTimeSettings();
	    	
			SimplePlaces sPlaces = new SimplePlaces(pnmlData);
			SimpleTransitions sTransitions = new SimpleTransitions(pnmlData);

	    	
	    	//PerformanceMeasure performanceMeasures;
			
    		// Create Progress Tab and set it as selected	
	    	if (progressPane == null)
	    	{
	    		progressPane = new ProgressBarHTMLPane("Analysis Progress");
	    		tabbedPane.addTab("Progress", progressPane);
	    	}

	    	
	    	if (resultsPanel == null)
	    		  resultsPanel = new JPanel();
	    	
	    	
	    	tabbedPane.setSelectedComponent(progressPane);
	    	
	    	
	    	String statusMesg="Tagged Net Converter ";
	    	
	    	
	    	if(pnmlData==null) return;
		      
	    	else if(!pnmlData.getPetriNetObjects().hasNext())
	    		statusMesg+="No Petri net objects defined!";
	    	
//	    	else if (selectedServer == -1)
//		    	statusMesg+="No server selected!";
	    	
//	    	else if (analysisSettings == null) 
//	    		statusMesg+= "Please check the analysis settings are valid.";
	    	
//	    	else if (sourceStateGroups == null)
//	    		statusMesg+= "No source states selected!";
	    	
//	    	else if (destinationStateGroups == null)
//	    		statusMesg+= "No destination states selected!";
	    	
	    	else
	    	{
//	    		ServerInfo serverInfo= serverPanel.getSelectedServer();
	    		
//	    		Analyse analyse = new Analyse(pnmlData, progressPane, resultsPanel);
//	    		analyse.setServer(serverInfo.getAddress(), serverInfo.getPort());
//	    		analyse.setPassageParameters(sourceStateGroups, destinationStateGroups, analysisSettings);
//	    		analyse.setTabbedPane(tabbedPane);	

	    		int clientNo = 0;
	    		String path = "/tmp";
	    		
	    		String filename = path + System.getProperty("file.separator") + "modFile" + clientNo + ".mod";
	    		
	    		TransMod translator = new TransMod(sPlaces, sTransitions, sourceStateGroups, destinationStateGroups, analysisSettings, filename, clientNo);
	
	    		File file = new File(filename);
	    		
	    		char[] chars = new char[(int) file.length()];
	    		
	    		try {
	    			BufferedReader reader = new BufferedReader(new FileReader(file));
	    			reader.read(chars);
	    			reader.close();
	    		} catch (FileNotFoundException e) {
	    			System.out.println("File Not Found " + e.getMessage());
	    		} catch (IOException e) {
	    			System.out.println("IO exce " + e.getMessage());
	    		}

	    		/*
	    		try {
	    				statusMesg = new String(new String(s.getBytes(), enc).getBytes("UTF-8"));
	    		} catch(UnsupportedEncodingException e) {
	    			System.out.println("StringUtility: Exception: " + e);
	    		}
	    			return new String();
	    		}
	    		*/
	    	
	    		statusMesg = new String(chars);

	    		
	    		// Start Analyse thread
//	    		Thread analyseTrd = new Thread(analyse);    		
//	    		analyseTrd.start();
//	    		return;
	    		
	    	}
	    	
	    	
	    	progressPane.setText(statusMesg);
	    }

	
	  };
	  
	  
	  /**
	   * This ActionListener responds to the buttons within the
	   * state group selection panel    
	   * i.e. the Add, Remove, Edit buttons
	   */
	  ActionListener stateButtonClick = new ActionListener()
	  {		  
		  public void actionPerformed(ActionEvent event)
		  {
			  if (event.getSource() == addBtn)
			  {
				  StateEditor addState = new StateEditor();
				  addState.setParent(TaggedModule.this);
				  addState.addState(pnmlData);
			  }
			  
			  else if (event.getSource() == removeBtn)
			  {
				  int startSelected[] = startStatesList.getSelectedIndices();
				  int endSelected[] = endStatesList.getSelectedIndices();
				  
				  int maxSize = startSelected.length + endSelected.length;
				  int[] removeList = new int [maxSize];
				  
				  // These loops merges the two sets of selected indices, removing duplicates
				  int i=0,j=0,k=0;
				  for(i =0; i< startSelected.length;i++)
				  {
					  while(j<endSelected.length && endSelected[j]<startSelected[i])
						  removeList[k++] = endSelected[j++];
					  
					  if(j<endSelected.length && endSelected[j] == startSelected[i])
						  j++;
						  
					  removeList[k++] = startSelected[i];
				  }				  
				  while(j<endSelected.length)
					  removeList[k++] = endSelected[j++];
				  
				  
				  // Remove the state groups
				  for(i=0; i<k; i++)
					  pnmlData.removeStateGroup(stateGroupData[ removeList[i] ]);
				  
				  // Update local array and UI  
				  updateStateLists(); 				 					
			  }
			  
			  else if (event.getSource() == editBtn)
			  {
				 int selectedState = startStatesList.getSelectedIndex();
				 
				 // If the state is not selected from the source states, retrieve from endStatesList 
				 if (selectedState == -1)
					 selectedState = endStatesList.getSelectedIndex();
				 
				 
				 if (selectedState != -1)
				 {
					 StateEditor editState = new StateEditor();
					 editState.setParent(TaggedModule.this);					 
					 editState.editState(pnmlData, stateGroupData[selectedState]);
				 }
				
			  }				
		  }
	  
	  };

	 
	
	  // Panel for Source and Destination state groups display and editing
	  private JPanel getStatesPanel()
	  {
		    JPanel statesPanel = new JPanel();
		    statesPanel.setBorder((new TitledBorder(new EtchedBorder(),"Source / Destination States")) );
		    statesPanel.setLayout(new BoxLayout(statesPanel, BoxLayout.PAGE_AXIS));
			
		    
		    // Create a panel for the labels
			JPanel labelPanel = new JPanel();
			labelPanel.setLayout(new GridLayout(1,2));
			labelPanel.add(new JLabel("Source States"));
			labelPanel.add(new JLabel("Destination States"));	
			labelPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,labelPanel.getPreferredSize().height));

						
		    startStatesList = new JList();
			endStatesList = new JList();
			populateLists();
			
			
			// Set the lists' alignment and selection properties
			startStatesList.setLayoutOrientation(JList.VERTICAL);
			startStatesList.setSelectionModel(new ToggleSelectionModel());
			startStatesList.setVisibleRowCount(-1);
			
			endStatesList.setLayoutOrientation(JList.VERTICAL);
			endStatesList.setSelectionModel(new ToggleSelectionModel());
			endStatesList.setVisibleRowCount(-1);
						
			// Add lists to scroller panes to allow scrolling of long lists
			JScrollPane startListScroller = new JScrollPane(startStatesList);
			JScrollPane endListScroller = new JScrollPane(endStatesList);
			
			// Set prefered UI size - this is changed by resizing the window
			startListScroller.setPreferredSize(new Dimension(300, 200));
			endListScroller.setPreferredSize(new Dimension(300, 200));
		
			
			// Create a panel to group the two lists
			JPanel stateListsPanel = new JPanel();
			stateListsPanel.setLayout(new GridLayout(1,2));
			stateListsPanel.add(startListScroller);
			stateListsPanel.add(endListScroller);
			
		
			// Create buttons
			addBtn = new JButton("Add State");
			addBtn.setMnemonic(KeyEvent.VK_A);
			addBtn.addActionListener(stateButtonClick);
			
			removeBtn = new JButton ("Remove State");
			removeBtn.setMnemonic(KeyEvent.VK_R);
			removeBtn.addActionListener(stateButtonClick);
			
			editBtn = new JButton ("Edit State");
			editBtn.setMnemonic(KeyEvent.VK_E);
			editBtn.addActionListener(stateButtonClick);
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(addBtn);
			buttonPanel.add(removeBtn);
			buttonPanel.add(editBtn);
			buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, buttonPanel.getPreferredSize().height ));

			
			// Add components to panel
			statesPanel.add(labelPanel);
			statesPanel.add(stateListsPanel );
			statesPanel.add(buttonPanel);
		
			
			return statesPanel;
	  }	

	  // This method loads the names of the state groups and adds them to the source / destination JLists
	  private void populateLists()
	  {
		  // Load the name of the state groups
		  String[] stateGrpNames = stateNames(stateGroupData);
		  
		  startStatesList.setListData(stateGrpNames);
		  endStatesList.setListData(stateGrpNames);
	  }
	  
	  
	private String[] stateNames(StateGroup[] states)
	{
		int size = states.length;
		String[] names = new String[size];
		
		for (int i=0; i<size; i++)
			names[i] = states[i].getName();
		
		return names;
		
	}
	
	void updateStateLists()
	{
		// refresh state group array
		stateGroupData = pnmlData.getStateGroups();
		
		// refresh the list of state group names
		populateLists();
	}


	
	// This method creates the panel for entering the required start and end time along with the step
	private JPanel getAnalysisPanel()
	{
		JPanel timePanel = new JPanel();
		
		timePanel.setBorder((new TitledBorder(new EtchedBorder(),"Analysis Settings")) );
		timePanel.setLayout(new GridLayout(1,2));
		
		JPanel startEndTime = new JPanel(new GridLayout(2,2));
		
		startEndTime.add(new JLabel("Start Time:") );
		startTime = new JTextField(4);
		startEndTime.add(startTime);
		
		startEndTime.add(new JLabel("End Time:") );
		endTime = new JTextField(4);
		startEndTime.add(endTime);
		
		startEndTime.setMaximumSize(new Dimension(Integer.MAX_VALUE, startEndTime.getPreferredSize().height));
		
		// Create time / method panel holder
		JPanel timeMethodPanel = new JPanel(new GridLayout(2,2));
		
		
		
		// Create time / method panel
		timeMethodPanel.add(new JLabel("Time Step:") );		
		timeStep = new JSpinner(new SpinnerNumberModel(0.10, 0.0, 100, 0.1) );		
		timeMethodPanel.add(timeStep);				

		timeMethodPanel.add(new JLabel("Method:"));
		
		methodCombo = new JComboBox();
		methodCombo.addItem("Laguerre");
		methodCombo.addItem("Euler");		
		timeMethodPanel.add(methodCombo);
		


		// Add to main analysis panel
		timePanel.add(startEndTime);
		timePanel.add(timeMethodPanel);
		timePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,timePanel.getPreferredSize().height));
		
		return timePanel;
	}
	
	/** This method retrieves the selected state groups from the entered JList
	 * and adds them to an ArrayList which is returned
	 * 
	 * @param stateList JList of selected states (source / destination lists)
	 * @return selectedstates ArrayList of the StateGroups that are selected; null if none are selected
	 */
	private ArrayList getSelectedStateGroups(JList statesList)
	{
		// Retrieve selected State Groups
		int[] selectedIndices = statesList.getSelectedIndices();
	
		if (selectedIndices.length > 0)
		{
			// Create output ArrayList
			ArrayList<StateGroup> selectedStates = new ArrayList<StateGroup>(selectedIndices.length);
			
			// Copy selected states from StateGroupData
			for (int i=0; i< selectedIndices.length; i++)
				selectedStates.add(stateGroupData[ selectedIndices[i] ]);
			
			return selectedStates;
		}
		
		else
			return null;
	}
	
	/**
	 * 
	 * @return AnalysisSetting object representing the selected options; null if any setting is invalid
	 */
	private AnalysisSetting getTimeSettings()
	{
    	String startString = startTime.getText();
    	String endString = endTime.getText();
    	Double step = (Double)timeStep.getValue();
    	String method = (String)methodCombo.getSelectedItem();
    	int numProcessors;
    	
    	try
    	{
    		double startT = Double.valueOf( startString.trim()).doubleValue();
    		double endT = Double.valueOf( endString.trim()).doubleValue();
    		double stepT = step.doubleValue();
    		
    		numProcessors = 1;//serverPanel.getNumProcessors();

    		// Final check that the values are appropriate
    		if (startT < endT && startT >= 0)
    			return new AnalysisSetting(startT, endT, stepT, method, numProcessors);
    		else
    			return null;
    	}
    	catch (NumberFormatException nfe)
    	{
	         return null;
	    }
			
	}
	
	  
}	  

  

// This class allows a JList to operate in a click toggle fashion - see JList java doc
class ToggleSelectionModel extends DefaultListSelectionModel
{
	private static final long serialVersionUID = 1L;
	boolean gestureStarted = false;
    
    public void setSelectionInterval(int index0, int index1)
    {
		if (isSelectedIndex(index0) && !gestureStarted) 
		    super.removeSelectionInterval(index0, index1);
		
		else 
		    super.setSelectionInterval(index0, index1);
		
		gestureStarted = true;
    }

    public void setValueIsAdjusting(boolean isAdjusting)
    {
    	if (isAdjusting == false) 
	    gestureStarted = false;
    }
}
