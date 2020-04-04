package utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Gantt-Chart representation.
 * @author Kelian Baert & Caroline de Pourtales
 */
public class GanttChart {
	// Rows of the chart, as lists of tasks
	private List<GanttTask>[] rows;
	
	/**
	 * Create a Gantt-Chart with the given number of rows.
	 * @param rows - The number of rows in the chart
	 */
	@SuppressWarnings("unchecked")
	public GanttChart(int rows) {
		this.rows = new ArrayList[rows];
		for(int i = 0; i < rows; i++)
			this.rows[i] = new ArrayList<GanttTask>();
	}
	
	/**
	 * Add a task to the Gantt-Chart
	 * @param row - A row index in the chart
	 * @param category - The category (color) of the task (note: two tasks of the same category cannot be executed in parallel)
	 * @param indexInCategory - The index of the task within its category (for display purposes only)
	 * @param time - The time at which this task starts being executed
	 * @param duration - The task duration
	 */
	public void addTask(int row, int category, int indexInCategory, int time, int duration) {
		// Add the new task to the corresponding row and keep the row sorted by time
		this.rows[row].add(new GanttTask(category, indexInCategory, time, duration));
		this.rows[row].sort((a,b) -> a.time - b.time);
	}

	/**
	 * Get the time at which all tasks are finished (i.e. the makespan).
	 * @return the end time
	 */
	public int getEndTime() {
		int endTime = 0;
		for(int i = 0; i < this.rows.length; i++) {
			if(this.rows[i].size() >= 1) {
				GanttTask lastTask = this.rows[i].get(this.rows[i].size() - 1);
				int rowEndTime = lastTask.time + lastTask.duration;
				if(rowEndTime > endTime)
					endTime = rowEndTime;
			}
		}
		return endTime;
	}
	
	/**
	 * Test the validity of the chart.
	 * @return true if this is a valid Gantt Chart, else false
	 */
	public boolean test() {
		HashMap<Integer, List<GanttTask>> byCategory = new HashMap<Integer, List<GanttTask>>();

		// Check that two tasks don't overlap in one row (and fill map of tasks by category)
		for(int i = 0; i < rows.length; i++) {
			List<GanttTask> row = rows[i];
					
			for(int j = 0; j < row.size() - 1; j++) {
				GanttTask t = row.get(j);
				
				if(t.time + t.duration > row.get(j+1).time)
					return false;
				
				if(!byCategory.containsKey(t.category))
					byCategory.put(t.category, new ArrayList<GanttTask>());
				byCategory.get(t.category).add(t);
			}
		}
		
		// Check that two tasks of the same category don't overlap
		for(int category : byCategory.keySet()) {
			List<GanttTask> tasks = byCategory.get(category);
			tasks.sort((a,b) -> a.time - b.time);
			
			for(int j = 0; j < tasks.size() - 1; j++) {
				GanttTask t = tasks.get(j);
				if(t.time + t.duration > tasks.get(j+1).time)
					return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		String str = "";
		for(int i = 0; i < rows.length; i++) {
			List<GanttTask> row = rows[i];
			
			String rowstr = "";
			for(int j = 0; j < row.size(); j++) {
				while(rowstr.length() < row.get(j).time)
					rowstr += " ";
				for(int k = 0; k < row.get(j).duration; k++)
					rowstr += row.get(j).category;
			}
			str += rowstr + (i < rows.length-1 ? "\n" : "");
		}
		return str;
	}
	
	/***
	 * Store a task in a Gantt-Chart
	 */
	static class GanttTask {
		private int category;
		private int indexInCategory;
		private int time;
		private int duration;
		
		public GanttTask(int category, int indexInCategory, int time, int duration) {
			this.category = category;
			this.time = time;
			this.duration = duration;
		}
	}

	/***
	 * Generate an image from this Gantt-Chart.
	 * @return an image representing this chart
	 */
	public BufferedImage generateImage() {

		/* TODO
		 * text into box
		 * CHECK
		 */
		
		int w = 1200, h = 900;
		
		BufferedImage bufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bufferedImage.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, w, h);
		Font stringFont = new Font( "SansSerif", Font.PLAIN, 20 );
		g.setFont(stringFont);
		g.setColor(Color.BLACK);

		//machines scale
		float machineScale = (h-30)/rows.length ;
		for (int i=0 ; i<rows.length ; i++) {
			String yLabel = "machine" + " " + i +"";
			g.drawLine(200,(int)(i*machineScale), w, (int)(i*machineScale));
			g.drawString(yLabel,10,i*machineScale +machineScale/2);
		}

		//time scale
		float timeScale = (w-280)/getEndTime() ;
		//draw the time line
		g.drawLine(200,h-15,w,h-15);
		for (int i=200 ; i < w-80 ; i++) {
			if (((i-200)/timeScale) % ((int)getEndTime()/10)==0) {
                g.setColor(Color.BLACK);
				String xLabel = (i-200)/timeScale + "";
				g.drawString(xLabel,i,h-10);
				g.drawLine(i,h-15,i,0);
			}
			if ((i-200)/timeScale==getEndTime()) {
                g.setColor(Color.BLACK);
                String xLabel = getEndTime() + "";
                g.drawString(xLabel,i,h-30);
				g.drawLine(i,h-15,i,0);
			}
		}

		//generate list of colors
		int njobs = rows[0].size();
		List<Color> colors = new ArrayList<Color>();
		Random Rand = new Random();
		for (int i=0 ; i< njobs ; i++) {
			float r = Rand.nextFloat();
			float green = Rand.nextFloat();
			float b = Rand.nextFloat();
			Color randomColor = new Color(r, green, b);
			colors.add(randomColor);
		}

		//fill the schedule
		for (int machine=0 ; machine<rows.length ; machine++) {
			for (GanttTask operation : rows[machine]) {
				g.setColor(colors.get(operation.category));
				g.fillRect((int) (200+operation.time*timeScale), (int)(machine*machineScale) , (int) (operation.duration*timeScale), (int) (machineScale));

				g.setColor(Color.BLACK);
				stringFont = new Font( "SansSerif", Font.PLAIN, 15 );
				g.setFont(stringFont);
				String label = operation.category + " . " + "num op" + "";
				int x = (int) (200+(operation.time+operation.duration/2)*timeScale);
				int y = (int) (machine*machineScale + machineScale/2);
				g.drawString(label,x,y);
			}
		}

		g.dispose();
		return bufferedImage;
	}
}
