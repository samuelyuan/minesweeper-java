import java.applet.Applet;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class Draw extends Applet implements Runnable, MouseListener
{
	//screen data
	private int screenWidth;
	private int screenHeight;
	private Image offscreenImage;
	private Graphics2D offscr;
	private int width, height;
	
	//runtime settings
	private Thread thread;
	private int delayTime = 50;
	
	//game graphics
	public static final int TILE_WIDTH = 20;
	public static final int TILE_HEIGHT = 20;
	private Image [] img = new Image[10];
	private final int IMGID_MINE = 0;
	private final int IMGID_FLAG = 1;
	private final int IMGID_FALSEFLAG = 2;
	private final int IMGID_HIDDENTILE = 3;
	private final int IMGID_REVEALEDTILE = 4;
	private final int IMGID_NEWGAME = 5;
	private final int IMGID_WIN = 6;
	private final int IMGID_LOSE = 7;
	private final int IMGID_TIMER = 8;
	private int numMilliseconds = 0;
	
	//game settings
	private MinesweeperBoard board;
	private final int NUM_ROWS = 9;
	private final int NUM_COLS = 9;
	private final int NUM_MINES = 10;
	
	public void init()
	{		
		//must have a thread
		thread = new Thread(this);
		thread.start();
		
		board = new MinesweeperBoard(NUM_ROWS, NUM_COLS, NUM_MINES);
		
		screenWidth = board.getNumCols() * TILE_WIDTH;
		screenHeight = (board.getNumRows()+1) * TILE_HEIGHT;
		
		setSize(screenWidth, screenHeight);
		setFocusable(true);
		addMouseListener(this);
		
		//screen
		width = getWidth();
		height = getHeight();
		offscreenImage = createImage(width, height);
		offscr = (Graphics2D) offscreenImage.getGraphics();
		
		loadImage();
	}
	
	public void loadImage()
	{
		try
		{
			img[IMGID_MINE] = getImage(getDocumentBase(), "img/mine.png");
			img[IMGID_FLAG] = getImage(getDocumentBase(), "img/flag.jpg");
			img[IMGID_HIDDENTILE] = getImage(getDocumentBase(), "img/hidden_tile.jpg");
			img[IMGID_REVEALEDTILE] = getImage(getDocumentBase(), "img/revealed_tile.jpg");
			img[IMGID_NEWGAME] = getImage(getDocumentBase(), "img/new_game.jpg");
			img[IMGID_FALSEFLAG] = getImage(getDocumentBase(), "img/false_flag.jpg");
			img[IMGID_WIN] = getImage(getDocumentBase(), "img/win.jpg");
			img[IMGID_LOSE] = getImage(getDocumentBase(), "img/lose.jpg");
			img[IMGID_TIMER] = getImage(getDocumentBase(), "img/timer.png");
		} 
		catch (Exception e) 
		{ 
			System.out.println(e.getMessage());
		}
	}
	
	public void update(Graphics g)
	{
		paint(g);
	}
	
	public void run() 
	{
		while (true)
		{
			//update screen
			repaint();
		
			//screen refresh
			try
			{
				Thread.sleep(delayTime);
				if (!board.isLose() && !board.isWin())
					numMilliseconds += delayTime;
			} catch (InterruptedException e) { }
		}
	}
	
	public void paint(Graphics g)
	{
		//clear back buffer to black
		offscr.setColor(Color.BLACK);
		offscr.fillRect(0, 0, width, height);
		
		offscr.drawImage(img[IMGID_NEWGAME], (board.getNumCols()/2)*TILE_WIDTH, (board.getNumRows())*TILE_HEIGHT, 
				TILE_WIDTH, TILE_HEIGHT, Color.WHITE, this);
		offscr.drawImage(img[IMGID_MINE], (board.getNumCols()/4)*TILE_WIDTH, (board.getNumRows())*TILE_HEIGHT, 
				TILE_WIDTH, TILE_HEIGHT, Color.WHITE, this);
		offscr.setColor(Color.WHITE);
		offscr.drawString("" + board.getNumMinesLeft(), (board.getNumCols()/4 + 1)*TILE_WIDTH, (board.getNumRows()+1)*TILE_HEIGHT);
		offscr.drawImage(img[IMGID_TIMER], (3*board.getNumCols()/4)*TILE_WIDTH, (board.getNumRows())*TILE_HEIGHT, 
				TILE_WIDTH, TILE_HEIGHT, Color.WHITE, this);
		offscr.setColor(Color.WHITE);
		offscr.drawString("" + numMilliseconds/1000, (3*board.getNumCols()/4 + 1)*TILE_WIDTH, (board.getNumRows()+1)*TILE_HEIGHT);
		
		drawBoard(board);
		checkBoardState(board);
		
		
		//send to front buffer
		g.drawImage(offscreenImage, 0, 0, this);
	}
	
	public void drawBoard(MinesweeperBoard b)
	{
		for (int r = 0; r < b.getNumRows(); r++)
		{
			for (int c = 0; c < b.getNumCols(); c++)
			{
				//draw initial square as hidden
				//offscr.setColor(Color.LIGHT_GRAY);
				//offscr.fillRect(c*TILE_WIDTH, r*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
				offscr.drawImage(img[IMGID_HIDDENTILE], c*TILE_WIDTH, r*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT, Color.WHITE, this);
				
				if (b.getIsRevealed(r, c) == true)
				{
					drawTile(b, r, c);
				}
				
				if (b.getIsFlagged(r, c) == true)
				{
					//offscr.setColor(Color.RED);
					//offscr.fillRect(c*TILE_WIDTH, r*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
					offscr.drawImage(img[IMGID_FLAG], c*TILE_WIDTH, r*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT, Color.WHITE, this);
				}
				
					
				if (b.isLose())	
				{
					if (b.getIsFlagged(r, c) == true)
					{
						if (b.getTile(r, c) != MinesweeperBoard.ID_MINE)
							offscr.drawImage(img[IMGID_FALSEFLAG], c*TILE_WIDTH, r*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT, Color.WHITE, this);

					}
				}
				
				//draw outline of square
				offscr.setColor(Color.BLACK);
				offscr.drawRect(c*TILE_WIDTH, r*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);		
			}
		}
	}
	
	public void checkBoardState(MinesweeperBoard b)
	{
		//check current state of board
		if (b.isLose())
		{
			//offscr.setColor(Color.BLACK);
			//offscr.drawString("GAME OVER :(!", 0, board.getNumRows()*TILE_HEIGHT/2);
			offscr.drawImage(img[IMGID_LOSE], (board.getNumCols()/2)*TILE_WIDTH, (board.getNumRows())*TILE_HEIGHT, 
					TILE_WIDTH, TILE_HEIGHT, Color.WHITE, this);
		}
		
		if (b.isWin())
		{
			b.flagAllMines();
			offscr.drawImage(img[IMGID_WIN], (board.getNumCols()/2)*TILE_WIDTH, (board.getNumRows())*TILE_HEIGHT, 
					TILE_WIDTH, TILE_HEIGHT, Color.WHITE, this);
			//offscr.setColor(Color.WHITE);
			//offscr.drawString("WIN :)!", 10, 10);
		}
	}
	
	public void drawTile(MinesweeperBoard b, int r, int c)
	{
		//offscr.setColor(Color.LIGHT_GRAY);
		//offscr.fillRect(c*TILE_WIDTH, r*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT);
		offscr.drawImage(img[IMGID_REVEALEDTILE], c*TILE_WIDTH, r*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT, Color.WHITE, this);
		
		if (b.getTile(r, c) == MinesweeperBoard.ID_MINE)
		{
			//offscr.setColor(Color.RED);
			//offscr.drawString("X", c*TILE_WIDTH + TILE_WIDTH/2, (r+1)*TILE_HEIGHT);
			offscr.drawImage(img[IMGID_MINE], c*TILE_WIDTH, r*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT, Color.WHITE, this);
		}
		else
		{
			if (b.getTile(r, c) > MinesweeperBoard.ID_EMPTY)
			{
				offscr.setColor(Color.CYAN);
				offscr.drawString("" + b.getTile(r, c), c*TILE_WIDTH, (r+1)*TILE_HEIGHT);
			}
		}
	}

	
	public void mouseClicked(MouseEvent e) 
	{	
		int r = e.getY()/TILE_HEIGHT;
		int c = e.getX()/TILE_WIDTH;
		
		if (c == board.getNumCols()/2 && r == board.getNumRows()
				&& e.getButton() == MouseEvent.BUTTON1)
		{
			board = new MinesweeperBoard(NUM_ROWS, NUM_COLS, NUM_MINES);
			numMilliseconds = 0;
			return;
		}
		
		if (board.isLose() == true || board.isWin() == true)
			return;
		
		if (!board.isValid(r, c))
			return;
		
		if (e.getButton() == MouseEvent.BUTTON1)
		{
			if (board.getIsFlagged(r, c) == false)
			{
				if (board.getTile(r, c) == MinesweeperBoard.ID_MINE)
				{
					revealAllMines(board);
				}
				else
				{
					if (board.getTile(r, c) == MinesweeperBoard.ID_EMPTY)
						board.floodFillEmptyTiles(r, c);
					else
						board.setIsRevealed(true, r, c);
				}
			}
		}

		if (e.getButton() == MouseEvent.BUTTON3)
		{
			if (board.getIsFlagged(r, c))
				board.setNumMinesLeft(board.getNumMinesLeft() + 1);
			else
				board.setNumMinesLeft(board.getNumMinesLeft() - 1);
			board.setIsFlagged(!board.getIsFlagged(r, c), r, c);
		}
	}
	
	public void revealAllMines(MinesweeperBoard b)
	{
		for (int r = 0; r < b.getNumRows(); r++)
		{
			for (int c = 0; c < b.getNumCols(); c++)
			{
				if (b.getTile(r, c) == MinesweeperBoard.ID_MINE)
				{
					board.setIsRevealed(true, r, c);
					if (b.getIsFlagged(r, c) == false)
					{
						offscr.drawImage(img[IMGID_REVEALEDTILE], c*TILE_WIDTH, r*TILE_HEIGHT, TILE_WIDTH, TILE_HEIGHT, Color.WHITE, this);
					}
				}
			}
		}
	}

	public void mouseEntered(MouseEvent arg0) { }
	public void mouseExited(MouseEvent arg0) {	}
	public void mousePressed(MouseEvent arg0) { }
	public void mouseReleased(MouseEvent arg0) { }


}
