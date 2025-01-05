
public class Board
{
	private int [][] boardData;
	private int numRows, numCols;
	
	public int getNumRows() { return numRows; }
	public int getNumCols() { return numCols; }
	public int getTile(int r, int c) { return boardData[r][c]; }
	public void setTile(int r, int c, int data) { boardData[r][c] = data; }
	
	public Board(int numRows, int numCols)
	{
		this.numRows = numRows;
		this.numCols = numCols;
		boardData = new int[numRows][numCols];
	
		reset();
	}
	
	public void reset()
	{
		for (int r = 0; r < numRows; r++)
			for (int c = 0; c < numCols; c++)
				boardData[r][c] = 0;
	}
	
	public boolean isValid(int r, int c)
	{
		if (r < 0 || r >= numRows || c < 0 || c >= numCols)
			return false;
		else
			return true;
	}

	public String toString()
	{
		String s = "Board Data:\n";
		for (int r = 0; r < numRows; r++)
		{
			for (int c = 0; c < numCols; c++)
				s += boardData[r][c] + " ";
			
			s += "\n";
		}
		
		return s;
	}
	
	public static void main(String[] args) 
	{
		Board board = new Board(9, 9);
		System.out.println(board);
	}

}
