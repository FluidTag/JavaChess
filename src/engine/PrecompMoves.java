package engine;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;

// Note: These moves only have the from (6 bits) and to (6 bits) defined, OR in piece (4 bits), captured (4 bits), isPromotion (4 bits), isEnPassant (4 bits), isCaslte (4 bits) in the 32 bit range
public class PrecompMoves {
	public static int[][][] precomputedMoves = new int[6][64][];
	// 0 - whitePawnPushes
	// 1 - blackPawnPushes
	// 2 - whitePawnCaptures
	// 3 - blackPawnCaptures
	// 4 - knightMoves
	// 5 - kingMoves
	
	public static long[][] precomputedMasks = new long[1][64];
	
	private static boolean withinBounds(byte row, byte col) {
		if (row < 0 || row > 7) return false;
		if (col < 0 || col > 7) return false;
		
		return true;
	}
	
	public static void init() {
		int[][] whitePawnPushes = new int[64][];
		int[][] blackPawnPushes = new int[64][];
		
		int[][] whitePawnCaptures = new int[64][];
		int[][] blackPawnCaptures = new int[64][];
		
		// Pawns
		for (byte square = 0; square < 64; square++) {
			byte row = (byte)Math.floor(square / 8);
			byte col = (byte)((byte)square % 8);
			
			for (byte c = 0; c <= 1; c++) {
				ArrayList<Integer> moves = new ArrayList<Integer>();
				ArrayList<Integer> captures = new ArrayList<Integer>();
				
				String color = (c == 0) ? "white" : "black";
				byte moveDir = (byte)((color == "white") ? 1 : -1);
				byte promotionRow = (byte)((color == "white") ? 7 : 0);
				String pushKey = color + "PawnPushes";
				String captureKey = color + "PawnCaptures";
				String originBoard = (color == "white") ? "whitePawns" : "blackPawns";
				
				if (row != promotionRow) {
					byte targetSquare = (byte)(square + (moveDir * 8));
					
					int move = square; // first 6 bits on the right
					move |= (targetSquare << 6); // next 6 bits
					
					moves.add(move);
				}
				
				byte doubleRow = (byte)((color == "white") ? 1 : 6);
				if (row == doubleRow) {
					byte targetSquare = (byte)(square + (moveDir * 16));
					int move = square;
					move |= (targetSquare << 6);
					
					moves.add(move);
				}
				
				if (withinBounds((byte)(row + moveDir), (byte)(col - moveDir))) {
					byte targetSquare = (byte)(square + (moveDir * 7));
					int move = square;
					move |= (targetSquare << 6);
					
					captures.add(move);
				}
				
				if (withinBounds((byte)(row + moveDir), (byte)(col + moveDir))) {
					byte targetSquare = (byte)(square + (moveDir * 9));
					int move = square;
					move |= (targetSquare << 6);
					
					captures.add(move);
				}
				
				int[] primitiveMoves = new int[moves.size()];
				for (int i = 0; i < moves.size(); i++) {
					primitiveMoves[i] = moves.get(i);
				}
				
				int[] primitiveCaptures = new int[captures.size()];
				for (int i = 0; i < captures.size(); i++) {
					primitiveCaptures[i] = captures.get(i);
				}
				
				if (color == "white") {
					whitePawnPushes[square] = primitiveMoves;
					whitePawnCaptures[square] = primitiveCaptures;
				} else { 
					blackPawnPushes[square] = primitiveMoves;
					blackPawnCaptures[square] = primitiveCaptures;
				}
			}
		}
		
		// Knights
		int[][] knightMoves = new int[64][];
		for (byte square = 0; square < 64; square++) {
			byte row = (byte)Math.floor(square / 8);
			byte col = (byte)(square % 8);
			
			byte[][] vectors = {
				{-2, -1}, {-2, 1}, {2, -1}, {2, 1},
				{-1, -2}, {-1, 2}, {1, -2}, {1, 2},
			};
			
			int[] moves = new int[8];
			int moveCount = 0;
			
			for (byte[] vector : vectors) {
				byte targetRow = (byte)(row + vector[0]);
				byte targetCol = (byte)(col + vector[1]);
				byte targetSquare = (byte)(targetRow * 8 + targetCol);
				
				if (!withinBounds(targetRow, targetCol)) continue;
				int move = square;
				move |= (targetSquare << 6);
				moves[moveCount++] = move;
			}
			
			knightMoves[square] = Arrays.copyOf(moves, moveCount);
		}
		
		// Kings
		int[][] kingMoves = new int[64][];
		
		for (byte square = 0; square < 64; square++) {
			byte row = (byte)(Math.floor(square / 8));
			byte col = (byte)(square % 8);
			long mask = 0L;
			
			byte[][] vectors = {
				{-1, 0}, {1, 0}, {0, -1}, {0, 1},
			    {-1, -1}, {-1, 1}, {1, -1}, {1, 1}	
			};
			
			int[] moves = new int[8];
			int moveCount = 0;
			
			for (byte[] vector : vectors) {
				byte targetRow = (byte)(row + vector[0]);
				byte targetCol = (byte)(col + vector[1]);
				byte targetSquare = (byte)(targetRow * 8 + targetCol);
				
				if (!withinBounds(targetRow, targetCol)) continue;
				
				int move = square;
				move |= (targetSquare << 6);
				mask |= (1L << targetSquare);
				
				precomputedMasks[0][square] = mask;
				
				moves[moveCount++] = move;
			}
			
			kingMoves[square] = Arrays.copyOf(moves, moveCount);
		}
		
		precomputedMoves[0] = whitePawnPushes;
		precomputedMoves[1] = blackPawnPushes;
		precomputedMoves[2] = whitePawnCaptures;
		precomputedMoves[3] = blackPawnCaptures;
		precomputedMoves[4] = knightMoves;
		precomputedMoves[5] = kingMoves;
	}
}