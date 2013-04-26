public class ONZFIX
	{
	/****************************************************************
	* NAME: main (of ONZFIX)										*
	* I/P:	args	-- command line arguments supplied by user		*
	*																*
	****************************************************************/
	
	public static void main(String[] args)
		{
		UserOptions options ;

		options = new UserOptions() ;
		if (!options.Process(args))
			{
			Message.error("\n***ERROR: " + options.getError()) ;
			options.PrintUsage() ;
			System.exit(0) ;
			}
		else
			{
			ROMProcessor	rom ;
			
			rom = new ROMProcessor() ;
			if (!rom.ProcessImage(options))
				{
				Message.error("\n***ERROR: " + rom.getError()) ;
				options.PrintUsage() ;
				System.exit(0) ;
				}
			
			}
			
		}

	}
