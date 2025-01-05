
public class MinesweeperBoard extends Board
{
	public static final int ID_MINE = -1;
	public static final int ID_EMPTY = 0;
	private boolean [][] isRevealed, isFlagged;
	private int numMinesLeft;
	
	public boolean getIsRevealed(int r, int c) { return isRevealed[r][c]; }
	public boolean getIsFlagged(int r, int c) { return isFlagged[r][c]; }
	public void setIsRevealed(boolean flag, int r, int c) { isRevealed[r][c] = flag; }
	public void setIsFlagged(boolean flag, int r, int c) { isFlagged[r][c] = flag; }
	public int getNumMinesLeft() { return numMinesLeft; }
	public void setNumMinesLeft(int numMinesLeft) { this.numMinesLeft = numMinesLeft; }
	
	public MinesweeperBoard(int numRows, int numCols, int numMines) 
	{
		super(numRows, numCols);
		generateMines(numMines);
		generateNumMinesAdjacent();
		
		numMinesLeft = numMines;
		isRevealed = new boolean[numRows][numCols];
		isFlagged = new boolean[numRows][numCols];
		for (int r = 0; r < numRows; r++)
		{
			for (int c = 0; c < numCols; c++)
			{
				isRevealed[r][c] = false;
				isFlagged[r][c] = false;
			}
		}
	}

	public void generateMines(int totalMines)
	{
		reset();
		
		for (int i = 0; i < totalMines; i++)
		{
			int randRow, randCol;
			do
			{
				randRow = (int)(Math.random() * getNumRows());
				randCol = (int)(Math.random() * getNumCols());
			} while (getTile(randRow, randCol) != 0);
			
			setTile(randRow, randCol, ID_MINE);
		}
	}
	
	public void generateNumMinesAdjacent()
	{
		for (int r = 0; r < getNumRows(); r++)
			for (int c = 0; c < getNumCols(); c++)
				setTile(r, c, generateNumMinesAdjacent(r, c));
	}
	
	public int generateNumMinesAdjacent(int rowNum, int colNum)
	{
		if (getTile(rowNum, colNum) == ID_MINE)
			return ID_MINE;
		
		int numMines = 0;
		for (int r = rowNum - 1; r <= rowNum + 1; r++)
		{
			for (int c = colNum - 1; c <= colNum + 1; c++)
			{
				if (r == rowNum && c == colNum)
					continue;
				
				if (isValid(r, c) && getTile(r, c) == ID_MINE)
					numMines++;
			}
		}
		
		return numMines;
	}
	
	public boolean isLose()
	{
		for (int r = 0; r < getNumRows(); r++)
		{
			for (int c = 0; c < getNumCols(); c++)
			{
				if (isRevealed[r][c] == true && getTile(r, c) == ID_MINE)
					return true;
			}
		}

		return false;
	}
	
	public boolean isWin()
	{
		int numMinesHidden = 0, numNonMinesRevealed = 0;
		for (int r = 0; r < getNumRows(); r++)
		{
			for (int c = 0; c < getNumCols(); c++)
			{
				if (isRevealed[r][c] == false && getTile(r, c) == ID_MINE)
					numMinesHidden++;
				
				if (isRevealed[r][c] == true && getTile(r, c) >= MinesweeperBoard.ID_EMPTY)
					numNonMinesRevealed++;
			}
		}
		
		if (numMinesHidden + numNonMinesRevealed == getNumRows()*getNumCols())
			return true;
		else
			return false;
	}
	
	public void flagAllMines()
	{
		numMinesLeft = 0;
		for (int r = 0; r < getNumRows(); r++)
		{
			for (int c = 0; c < getNumCols(); c++)
			{
				if (getTile(r, c) == ID_MINE)
				{
					isFlagged[r][c] = true;
				
				}
			}
		}
	}
	
	public void floodFillEmptyTiles(int centerRowNum, int centerColNum)
	{
		if (!isValid(centerRowNum, centerColNum))
			return;
		
		if (getTile(centerRowNum, centerColNum) != ID_EMPTY && isFlagged[centerRowNum][centerColNum] == false)
			isRevealed[centerRowNum][centerColNum] = true;
		
		if (isRevealed[centerRowNum][centerColNum] == false && isFlagged[centerRowNum][centerColNum] == false)
			isRevealed[centerRowNum][centerColNum] = true;
		else
			return;
		
		for (int r = centerRowNum-1; r <= centerRowNum+1; r++)
		{
			for (int c = centerColNum-1; c <= centerColNum+1; c++)
			{
				if (r == centerRowNum && c == centerColNum)
					continue;
				
				floodFillEmptyTiles(r, c);
			}
		}
	}
}
