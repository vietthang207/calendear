package calendear.action;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Stack;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import calendear.util.Command;
import calendear.util.CommandAdd;
import calendear.util.CommandDelete;
import calendear.util.CommandDisplay;
import calendear.util.CommandUpdate;
import calendear.util.CMD_TYPE;
import calendear.util.Task;
import calendear.util.TASK_TYPE;
import calendear.storage.DataManager;
/**
 * 
 * @author Wu XiaoXiao
 *
 */
public class Action {
	private static final Logger log= Logger.getLogger( "Action" );
	private ArrayList<Task> _data;
	private Stack<Command> _undoStack;
	private Stack<Command> _redoStack;
	private DataManager _dm; 
	
	//constructor
	/**
	 * default constructor, not used.
	 */
	public Action(){
		_data = new ArrayList<Task>();
		_undoStack = new Stack<Command>();
		_redoStack = new Stack<Command>();
	}
	
	public Action(String nameOfFile) throws ParseException {
		_undoStack = new Stack<Command>();
		_redoStack = new Stack<Command>();
		_dm = new DataManager(nameOfFile);
		_data = _dm.buildData();
	}
	
	//not using
	public Action(ArrayList<Task> tasks, String nameOfFile) {
		_data = tasks;
		_undoStack = new Stack<Command>();
		_redoStack = new Stack<Command>();
		_dm = new DataManager(nameOfFile);
	}
	/**
	 * returns the task that was added.
	 * id is arrayList.size() - 1
	 * @param c
	 * @return
	 */
	public Task exeAdd(CommandAdd c){
		assertCommandNotNull(c);
		Task addedTask = c.getTask();
		_data.add(addedTask);
		c.setTask(null);
		_undoStack.push(c);
		_dm.updateData(getNoNullArr());
		return addedTask;
	}
	/**
	 * remove task with id
	 * @param id
	 */
	public Task exeDelete(CommandDelete c){
		assertCommandNotNull(c);
		int id = c.getIndex();
		Task t = _data.get(id);
		_data.set(id, null);
		c.setDeletedTask(t);
		_undoStack.push(c);
		_dm.updateData(getNoNullArr());
		return t;
	}

	//helper class to exchange contents of CommandUpdate and task
	private void updateInformation(CommandUpdate c, Task toUpdate){
		int changeId = c.getIndex();
		final int NAME_ID = 0;
		final int TYPE_ID = 1;
		final int STARTT_ID = 2;
		final int ENDT_ID = 3;
		final int LOCATION_ID = 4;
		final int NOTE_ID = 5;
		final int TAG_ID = 6;
		final int IMP_ID = 7;//important
		final int COMP_ID = 8;//finished
		
		try{
			
			boolean[] u = c.getChecklist();//refactor to isChanged in the future
			Object[] i = c.getNewInfo();
			
			if(u[NAME_ID]){
				//name
				String oldName = toUpdate.getName();
				toUpdate.setName((String)i[NAME_ID]);
				i[NAME_ID] = oldName;
			}
			if(u[TYPE_ID]){
				//type
				TASK_TYPE oldType = toUpdate.getType();
				toUpdate.setType((TASK_TYPE)i[TYPE_ID]);
				i[TYPE_ID] = (Object)oldType;
			}
			if(u[STARTT_ID]){
				//start time
				GregorianCalendar oldStartTime = toUpdate.getStartTime();
				toUpdate.setStartTime((GregorianCalendar) i[STARTT_ID]);
				i[STARTT_ID] = (Object)oldStartTime;
			}
			if(u[ENDT_ID]){
				//end time
				GregorianCalendar oldEndTime = toUpdate.getEndTime();
				toUpdate.setEndTime((GregorianCalendar) i[ENDT_ID]);
				i[ENDT_ID] = (Object)oldEndTime;
			}
			if(u[LOCATION_ID]){
				String newLoc = (String)i[LOCATION_ID];
				i[LOCATION_ID] = toUpdate.getLocation();
				toUpdate.setLocation(newLoc);
			}
			if(u[NOTE_ID]){
				String newNote = (String)i[NOTE_ID];
				i[LOCATION_ID] = toUpdate.getLocation();
				toUpdate.setLocation(newNote);
			}
			if(u[TAG_ID]){
				//can be a series of tags, need to specify 
				// add/delete/replace
				//TODO
			}
			if(u[IMP_ID]){
				boolean isImportant = (boolean)i[IMP_ID];
				i[IMP_ID] = Boolean.toString(toUpdate.isImportant());
				toUpdate.markImportant(isImportant);
			}
			if(u[COMP_ID]){
				boolean isFinished = (boolean)i[COMP_ID];
				i[COMP_ID] = Boolean.toString(toUpdate.isFinished());
				toUpdate.setIsFinished(isFinished);
			}
			
			_undoStack.add(c);
			_dm.updateData(getNoNullArr());
		}catch (NullPointerException e){
			e.printStackTrace();
		}catch (ArrayIndexOutOfBoundsException e){
			e.printStackTrace();
		}

	}

	public Task exeUpdate(CommandUpdate c){
		assertCommandNotNull(c);
		int changeId = c.getIndex();
		Task toUpdate = _data.get(changeId);
		
		updateInformation(c, toUpdate);
		this._dm.updateData(getNoNullArr());
		return toUpdate;
	}
	
	/**
	 * 
	 * @param c
	 * @return arrayList containing tasks to show, all null elements should not be shown
	 */
	public ArrayList<Task> exeDisplay(CommandDisplay c){
		assertCommandNotNull(c);
		if(c.isOnlyImportantDisplayed()){
			//prepares an arraylist where all non important tasks are represented as null
			ArrayList<Task> imp = new ArrayList<Task>();
			for(int i = 0; i< _data.size(); i++){
				Task temp = _data.get(i);
				if(temp != null && temp.isImportant()){
					imp.add(temp);
				}else{
					imp.add(null);
				}
			}
			return imp;
		}else{
			return _data;
		}
	}
	
	public void exeSearch(String str){
		
	}
	
	public void exeUndo(){
		Command previousCmd = _undoStack.pop();
		CMD_TYPE cmdType = previousCmd.getType();
		switch(cmdType){
			case ADD: 
				//remove the newly added task
				_data.remove(_data.size()-1);
				break;
			case DISPLAY:
				//lol what am i doing,commandDisplay is not recorded
				break;
			case UPDATE:
				//TODO
				break;
			case DELETE:
				//TODO
				break;
			case SEARCH:
				//TODO
				break;
			case SORT:
				//TODO
				break;
			case MARK:
				//TODO
				break;
			case DONE:
				//TODO
				break;
			case UNDO:
				//TODO
				break;
			case TAG:
				//TODO
				break;
			case LINK_GOOGLE:
				//TODO
				break;
			default:
				assert(false): "received wrong command in action.exeUndo";
				log.log(Level.SEVERE, previousCmd.toString(), previousCmd);
		}
		_redoStack.push(previousCmd);
		this._dm.updateData(getNoNullArr());
		log.log(Level.FINE, "pushed previousCmd to redoStack", previousCmd);
	}
	
	public void exeTag(){
		this._dm.updateData(getNoNullArr());
	}
	
	public void exeMark(){
		this._dm.updateData(getNoNullArr());
	}
	
	public void exeSort(){
		this._dm.updateData(getNoNullArr());
	}
	
	public void exeExit(){
		this._dm.updateData(getNoNullArr());
	}
	
	//--------------------------------------------------------------------------
	//helper
	/**
	 * returns an array of data with null objects removed
	 * @return
	 */
	private ArrayList<Task> getNoNullArr(){
		ArrayList<Task> toReturn = new ArrayList<Task>();
		Task t=  null;
		for(int i = 0; i<_data.size(); i++){
			t = _data.get(i);
			if(t != null){
				toReturn.add(t);
			}
		}
		return toReturn;
	}
	
	private void assertCommandNotNull(Command c){
		assert(c != null): "Received null command";
	}
}


