import java.io.* ;
import java.lang.String ;

public class ROMProcessor
	{
	private final static int	k_BYTE_NINTENDO_LOGO = 0x0104 ;		// start address of nintendo logo in rom image
	private final static int	k_BYTE_CARTRIDGE_TITLE = 0x0134 ;	// start address of cartridge title in rom image
	private final static int	k_BYTE_GBC_MODE = 0x0143 ;			// address of dmg/gbc compatibility/gbc only flag in rom image
	private final static int	k_BYTE_LICENSEE_CODE_MSB = 0x0144 ;	// address of licensee code msb in rom image
	private final static int	k_BYTE_LICENSEE_CODE_LSB = 0x0145 ;	// address of licensee code lsb in rom image
	private final static int	k_BYTE_SGB_FEATURES = 0x0146 ;		// address of super gameboy flag in rom image
	private final static int	k_BYTE_CARTRIDGE_TYPE = 0x00147 ;	// address of mbc type in rom image
	private final static int	k_BYTE_ROM_SIZE = 0x0148 ;			// address of rom size in rom image
	private final static int	k_BYTE_RAM_SIZE = 0x0149 ;			// address of ram size in rom image
	private final static int	k_BYTE_COUNTRY_CODE = 0x014A ;		// address of country/region code in rom image
	private final static int	k_BYTE_LICENSEE_CODE = 0x014B ;		// address of manufacturer/publisher/licensee in in rom image
	private final static int	k_BYTE_VERSION = 0x014C ;			// address of rom version in rom image
	private final static int	k_BYTE_COMPLEMENT = 0x014D ;		// address of cartridge complement checksum in rom image
	private final static int	k_BYTE_CHECKSUM_MSB = 0x014E ;		// address of lsb of cartridge checksum in rom image
	private final static int	k_BYTE_CHECKSUM_LSB = 0x014F ;		// address of msb of cartridge checksum in rom image

	private final static int	k_MIN_ROM_SIZE = 0x150 ;		// minimum legal size that a rom image can possibly be
	private final static int	k_ROM_BANK_SIZE = 16384 ;		// size of a gameboy rom bank (in bytes)
	
	private	byte	m_romImage[] ;								// gameboy rom image that is being manipulated
	
	private boolean	m_debug ;									// debug only flag
	
	private final static short	m_nintendoLogo[]={	0xCE,0xED,0x66,0x66,0xCC,0x0D,0x00,0x0B,0x03,0x73,0x00,0x83,0x00,0x0C,0x00,0x0D,
													0x00,0x08,0x11,0x1F,0x88,0x89,0x00,0x0E,0xDC,0xCC,0x6E,0xE6,0xDD,0xDD,0xD9,0x99,
													0xBB,0xBB,0x67,0x63,0x6E,0x0E,0xEC,0xCC,0xDD,0xDC,0x99,0x9F,0xBB,0xB9,0x33,0x3E } ;

	private String	m_errorMsg ;								// last error encountered by rom processor


	/****************************************************************
	* NAME: ROMProcessor (constructor)								*
	*																*
	*																*
	****************************************************************/
	
	public ROMProcessor()
		{
		// set last error message to "no error"
		m_errorMsg = "No error" ;
		// disable debug option
		m_debug = false ;
		}
	

	private String getHex(int value, int length)
		{
		String	s ;
		
		s = Integer.toHexString(value) ;
		while (s.length() < length)
			{
			s = "0" + s ;
			}
		
		s = s.substring(s.length()-length) ;
		
		return (s) ;
		}
	
	/****************************************************************
	* NAME: getError (of ROMProcessor)								*
	* RET:	String	-- error message of last error encountered		*
	*																*
	****************************************************************/
	
	public String getError()
		{
		// return message of last error encountered
		return (m_errorMsg) ;
		}
	
	
	/****************************************************************
	* NAME: ReadImage (of ROMProcessor)								*
	* I/P:	filename	-- filename of rom image to input			*
	*																*
	* This function reads in an entire rom image for processing. It	*
	* verifies that the file exists, verifies that it can be read &	*
	* written to, creates an array of appropriate size and reads in	*
	* the stream of bytes.											*
	*																*
	****************************************************************/
	
	private boolean ReadImage(String filename)
		{
		File	imageFile ;
		
		DataInputStream	imageStream ;
		
		Debug.assert(filename.length() != 0, "Filename must be supplied for ReadImage to function") ;
		imageFile = new File(filename) ;

		// verify that file exists
		if (!imageFile.exists())
			{
			m_errorMsg = "File does not exist" ;
			
			return (false) ;
			}

		if (!imageFile.isFile())
			{
			m_errorMsg = "Specified image is not a file" ;
			
			return (false) ;
			}
		
		if ((!imageFile.canRead()) || (!imageFile.canWrite()))
			{
			m_errorMsg = "File cannot be read and/or written" ;
			
			return (false) ;
			}
		
		try
			{
			imageStream = new DataInputStream(new FileInputStream(imageFile)) ;
			}
		
		catch (FileNotFoundException fnfEX)
			{
			m_errorMsg = "Specified ROM image was not found" ;
			
			return (false) ;
			}
		
		m_romImage = new byte[(int)(imageFile.length())] ;
		try
			{
			imageStream.read(m_romImage) ;
			}
		
		catch (IOException ioEx)
			{
			m_errorMsg = "Error while reading from specified ROM image" ;
				
			return (false) ;
			}
		
		try
			{
			imageStream.close() ;
			}
		
		catch (IOException ioEx)
			{
			m_errorMsg = "Failed to read from specified ROM image correctly" ;
			
			return (false) ;
			}
		
		if (m_romImage.length < k_MIN_ROM_SIZE)
			{
			m_errorMsg = "Specified ROM image is smaller than " + k_MIN_ROM_SIZE + " bytes." ;
				
			return (false) ;
			}
		
		return (true) ;
		}
	
	
	/****************************************************************
	* NAME: WriteImage (of ROMProcessor)							*
	* I/P:	filename	-- filename of rom image to output			*
	*																*
	* This function writes out a rom image that has been processed.	*
	* It takes a single parameter of the rom image filename that is *
	* to be output. It verifies that the file can be written out,	*
	* writes the bytes, and reports an error if the output fails.	*
	*																*
	****************************************************************/
	
	public boolean WriteImage(String filename)
		{
		File	imageFile ;
		
		DataOutputStream	imageStream ;
		
		// assert that rom image > minimum possible rom size
		Debug.assert(m_romImage.length >= k_MIN_ROM_SIZE, "ROM Image is smaller than " + k_MIN_ROM_SIZE + " bytes.") ;
		Debug.assert(filename.length() != 0, "Filename must be supplied for WriteImage to function") ;
		imageFile = new File(filename) ;

		if (!imageFile.canWrite())
			{
			m_errorMsg = "File cannot be written" ;
			
			return (false) ;
			}
		
		if (!m_debug)
			{
			try
				{
				imageStream = new DataOutputStream(new FileOutputStream(imageFile)) ;
				}
		
			catch (IOException ioEX)
				{
				m_errorMsg = "Could not open specified ROM image to write" ;

				return (false) ;
				}


			try
				{
				imageStream.write(m_romImage, 0, m_romImage.length) ;
				}
		
			catch (IOException ioEx)
				{
				m_errorMsg = "Error while writing to specified ROM image" ;
					
				return (false) ;
				}
		
			try
				{
				imageStream.close() ;
				}
		
			catch (IOException ioEx)
				{
				m_errorMsg = "Failed to write to specified ROM image correctly" ;
				
				return (false) ;
				}

			}
		
		return (true) ;
		}
	
	
	/****************************************************************
	* NAME: ProcessImage (of ROMProcessor)							*
	*																*
	*																*
	****************************************************************/
	
	public boolean ProcessImage(UserOptions options)
		{
		m_debug = options.isDebugOnly() ;

		// if debug option enabled display debug message
		if (m_debug)
			{
			Message.println("Debug option enabled...") ;
			m_debug = true ;
			}
		
		// read rom image
		if (!ReadImage(options.getROMImageFilename()))
			{
			return (false) ;
			}
		
		// if pad option specified pad image
		if (options.isPadImage())
			{
			PadImage(options.getPadValue()) ;
			}
		
		if (options.isTruncateImage())
			{
			TruncateImage() ;
			}
		
		if (options.isCartridgeTitle())
			{
			SetCartridgeTitle(options.getCartridgeTitle()) ;
			}
		
		if (options.isGBCCompatible())
			{
			SetGBCCompatible() ;
			}
		
		if (options.isGBCOnly())
			{
			SetGBCOnly() ;
			}
		
		if (options.isMBCType())
			{
			SetMBCType(options.getMBCType()) ;
			}
		
		if (options.isRAMSize())
			{
			SetRAMSize(options.getRAMSize()) ;
			}
		
		if (options.isValidateImage())
			{
			ValidateImage() ;
			}
		
		// write modified image back out
		if (!WriteImage(options.getROMImageFilename()))
			{
			return (false) ;
			}
		
		return (true) ;
		}
	
	
	/****************************************************************
	* NAME: TruncateImage (of ROMProcessor)							*
	*																*
	*																*
	****************************************************************/
	
	public void TruncateImage()
		{
		byte	truncatedImage[] ;
		
		int	truncatedImageSize ;
		int	i ;
		
		// assert that rom image > minimum possible rom size
		Debug.assert(m_romImage.length >= k_MIN_ROM_SIZE, "ROM Image is smaller than " + k_MIN_ROM_SIZE + " bytes.") ;

		truncatedImageSize = 256*32768 ;
		while (m_romImage.length<truncatedImageSize)
			{
			truncatedImageSize /= 2 ;
			}

		if (truncatedImageSize < m_romImage.length)
			{
			Message.println("Truncating to " + truncatedImageSize/1024 + "kB") ;
			truncatedImage = new byte[truncatedImageSize] ;
			for (i=0; i<truncatedImageSize; i++)
				{
				truncatedImage[i] = m_romImage[i] ;
				}
			
			m_romImage = new byte[truncatedImageSize] ;
			m_romImage = truncatedImage ;
			}
		
		}
	
	
	/****************************************************************
	* NAME: PadImage (of ROMProcessor)								*
	*																*
	*																*
	****************************************************************/
	
	public void PadImage(byte padValue)
		{
		byte	paddedImage[] ;

		int	bytesToAdd ;
		int	paddedImageSize ;
		int	i ;
		
		// assert that rom image > minimum possible rom size
		Debug.assert(m_romImage.length >= k_MIN_ROM_SIZE, "ROM Image is smaller than " + k_MIN_ROM_SIZE + " bytes.") ;

		paddedImageSize = 0x8000 ;
		while (m_romImage.length > paddedImageSize)
			{
			paddedImageSize *= 2 ;
			}

		if (paddedImageSize > m_romImage.length)
			{
			bytesToAdd = paddedImageSize - m_romImage.length ;
			Message.println("Padding to " + paddedImageSize/1024 + "KBytes with pad value 0x" + getHex(padValue, 2)) ;
			paddedImage = new byte[paddedImageSize] ;
			for (i=0; i<paddedImageSize; i++)
				{
				paddedImage[i] = padValue ;
				}
		
			for (i=0; i<m_romImage.length; i++)
				{
				paddedImage[i] = m_romImage[i] ;
				}
			
			m_romImage = paddedImage ;
			Message.println("\tAdded " + bytesToAdd + " bytes") ;
			}
		else
			{
			Message.println("\tNo padding needed") ;
			}

		}
	
	
	/****************************************************************
	* NAME: SetCartridgeTitle (of ROMProcessor)						*
	*																*
	*																*
	****************************************************************/
	
	public void SetCartridgeTitle(String cartridgeTitle)
		{
		// assert that rom image > minimum possible rom size
		Debug.assert(m_romImage.length >= k_MIN_ROM_SIZE, "ROM Image is smaller than " + k_MIN_ROM_SIZE + " bytes.") ;
		Debug.assert(cartridgeTitle.length() != 0, "Blank cartridge title supplied") ;

		Message.println("Setting cartridge title:") ;
		int	i ;
			
		byte asciiName[] = cartridgeTitle.getBytes() ;
			
		for (i=0; i<asciiName.length; i++)
			{
			m_romImage[k_BYTE_CARTRIDGE_TITLE+i] = asciiName[i] ;
			}

		Message.println("\tTitle set to " + cartridgeTitle) ;
		}
	
	
	/****************************************************************
	* NAME: SetGBCCompatible (of ROMProcessor)						*
	*																*
	*																*
	****************************************************************/
	
	public void SetGBCCompatible()
		{
		// assert that rom image > minimum possible rom size
		Debug.assert(m_romImage.length >= k_MIN_ROM_SIZE, "ROM Image is smaller than " + k_MIN_ROM_SIZE + " bytes.") ;

		Message.println("Setting Colour GameBoy compatible mode") ;
		m_romImage[k_BYTE_GBC_MODE] = new Short((short)(0x80)).byteValue() ;
		Message.println("\tColour GameBoy compatible mode set") ;
		}


	/****************************************************************
	* NAME: SetGBCOnly (of ROMProcessor)							*
	*																*
	*																*
	****************************************************************/
	
	public void SetGBCOnly()
		{
		// assert that rom image > minimum possible rom size
		Debug.assert(m_romImage.length >= k_MIN_ROM_SIZE, "ROM Image is smaller than " + k_MIN_ROM_SIZE + " bytes.") ;

		Message.println("Setting Colour GameBoy only mode") ;
		m_romImage[k_BYTE_GBC_MODE] = new Short((short)(0xC0)).byteValue() ;
		Message.println("\tColour GameBoy only mode set") ;
		}
	
	
	/****************************************************************
	* NAME: SetMBCType (of ROMProcessor)							*
	*																*
	*																*
	****************************************************************/
	
	public void SetMBCType(byte mbcType)
		{
		// assert that rom image > minimum possible rom size
		Debug.assert(m_romImage.length >= k_MIN_ROM_SIZE, "ROM Image is smaller than " + k_MIN_ROM_SIZE + " bytes.") ;

		// report that the mbc type is being changed
		Message.println("Setting MBC Type") ;
		// set cartridge mbc type to specified type
		m_romImage[k_BYTE_CARTRIDGE_TYPE] = mbcType ;
		// report that mbc type has been changed
		Message.println("\tMBC Type set to 0x" + getHex(mbcType, 2)) ;
		// report specified mbc type as a human readable text message
		DescribeMBCType(mbcType) ;
		}
	
	
	/****************************************************************
	* NAME: SetRAMSize (of ROMProcessor)							*
	*																*
	*																*
	****************************************************************/
	
	public void SetRAMSize(byte ramSize)
		{
		// assert that rom image > minimum possible rom size
		Debug.assert(m_romImage.length >= k_MIN_ROM_SIZE, "ROM Image is smaller than " + k_MIN_ROM_SIZE + " bytes.") ;

		Message.println("Setting RAM Size") ;
		m_romImage[k_BYTE_RAM_SIZE] = ramSize ;
		Message.println("\tRAM Size set to 0x" + getHex(ramSize, 2)) ;
		}

	
	/****************************************************************
	* NAME: DescribeMBCType (of ROMProcessor)						*
	*																*
	*																*
	****************************************************************/
	
	public void DescribeMBCType(short mbcType)
		{
		String	mbcDescription ;
		
		switch (mbcType)
			{
			case 0x00 :
				mbcDescription = "No MBC -- ROM Only" ;
				break ;
			case 0x01 :
				mbcDescription = "MBC 0x01 -- ROM Only" ;
				break ;
			case 0x02 :
				mbcDescription = "MBC 0x01 -- ROM & RAM" ;
				break ;
			case 0x03 :
				mbcDescription = "MBC 0x01 -- ROM & RAM & Battery" ;
				break ;
			case 0x05 :
				mbcDescription = "MBC 0x02 -- ROM Only" ;
				break ;
			case 0x06 :
				mbcDescription = "MBC 0x02 -- ROM & Battery" ;
				break ;
			case 0x08 :
				mbcDescription = "ROM & RAM" ;
				break ;
			case 0x09 :
				mbcDescription = "ROM & RAM & Battery" ;
				break ;
			case 0x0B :
				mbcDescription = "MMM01 -- ROM Only" ;
				break ;
			case 0x0C :
				mbcDescription = "MMM01 -- ROM & RAM" ;
				break ;
			case 0x0D :
				mbcDescription = "MMM01 -- ROM & RAM & Battery" ;
				break ;
			case 0x0F :
				mbcDescription = "MBC 0x03 -- ROM & Timer & Battery" ;
				break ;
			case 0x10 :
				mbcDescription = "MBC 0x03 -- ROM & RAM & Timer & Battery" ;
				break ;
			case 0x11 :
				mbcDescription = "MBC 0x03 -- ROM Only" ;
				break ;
			case 0x12 :
				mbcDescription = "MBC 0x03 -- ROM & RAM" ;
				break ;
			case 0x13 :
				mbcDescription = "MBC 0x03 -- ROM & RAM & Battery" ;
				break ;
			case 0x15 :
				mbcDescription = "MBC 0x04 -- ROM Only" ;
				break ;
			case 0x16 :
				mbcDescription = "MBC 0x04 -- ROM & RAM" ;
				break ;
			case 0x17 :
				mbcDescription = "MBC 0x04 -- ROM & RAM & Battery" ;
				break ;
			case 0x19 :
				mbcDescription = "MBC 0x05 -- ROM Only" ;
				break ;
			case 0x1A :
				mbcDescription = "MBC 0x05 -- ROM & RAM" ;
				break ;
			case 0x1B :
				mbcDescription = "MBC 0x05 -- ROM & RAM & Battery" ;
				break ;
			case 0x1C :
				mbcDescription = "MBC 0x05 -- ROM & Rumble" ;
				break ;
			case 0x1D :
				mbcDescription = "MBC 0x05 -- ROM & RAM & Rumble" ;
				break ;
			case 0x1E :
				mbcDescription = "MBC 0x05 -- ROM & RAM & Battery & Rumble" ;
				break ;
			case 0xFC :
				mbcDescription = "Pocket Camera" ;
				break ;
			case 0xFD :
				mbcDescription = "Bandai TAMA5" ;
				break ;
			case 0xFE :
				mbcDescription = "HuC 3" ;
				break ;
			case 0xFF :
				mbcDescription = "HuC 1 -- ROM & RAM & Battery";
				break ;
			default :
				mbcDescription = "Unknown" ;
				break ;
				
			}

		Message.println("\t\t" + mbcDescription) ;
		}

	
	/****************************************************************
	* NAME: ValidateNintendoLogo (of ROMProcessor)					*
	*																*
	* This function validates the 48 bytes of Nintendo logo			*
	* information at address 0x104 in the ROM image. It assumes		*
	* the ROM image is at least the size of a standard ROM header,	*
	* which is 0x14F bytes.											*
	*																*
	*																*
	****************************************************************/
	
	public void ValidateNintendoLogo()
		{
		int	i ;
		int	bytesChanged ;
		
		// assert that rom image > minimum possible rom size
		Debug.assert(m_romImage.length >= k_MIN_ROM_SIZE, "ROM Image is smaller than " + k_MIN_ROM_SIZE + " bytes.") ;

		bytesChanged = 0 ;
		for (i=0; i<m_nintendoLogo.length; i++)
			{
			byte headerByte = new Short(m_nintendoLogo[i]).byteValue() ;
			if (m_romImage[k_BYTE_NINTENDO_LOGO + i] != headerByte)
				{
				bytesChanged++ ;
				m_romImage[k_BYTE_NINTENDO_LOGO + i] = headerByte ;
				}
			
			}

		if (bytesChanged!=0)
			{
			Message.println("\tChanged " + bytesChanged + " bytes in the Nintendo Character Area") ;
			}
		else
			{
			Message.println("\tNintendo Character Area is OK") ;
			}

		}
	

	/****************************************************************
	* NAME: ValidateROMSize (of ROMProcessor)						*
	*																*
	*																*
	****************************************************************/
	
	public void ValidateROMSize()
		{
		byte	currentROMSizeValue ;
		byte	calculatedROMSize ;
		int	actualROMSize ;
		
		// assert that rom image > minimum possible rom size
		Debug.assert(m_romImage.length >= k_MIN_ROM_SIZE, "ROM Image is smaller than " + k_MIN_ROM_SIZE + " bytes.") ;

		currentROMSizeValue = m_romImage[k_BYTE_ROM_SIZE] ;
		actualROMSize = m_romImage.length ;
		calculatedROMSize = 0 ;
		while (actualROMSize>(0x8000L<<calculatedROMSize))
			{
			calculatedROMSize++ ;
			}

		if (calculatedROMSize != currentROMSizeValue)
			{
			m_romImage[k_BYTE_ROM_SIZE] = calculatedROMSize ;
			Message.println("\tChanged ROM size byte from 0x" + getHex(currentROMSizeValue, 2) +
				" (" + (0x8000L<<currentROMSizeValue)/1024 + "kB) to 0x" + getHex(calculatedROMSize, 2) +
				" (" + (0x8000L<<calculatedROMSize)/1024 + "kB)") ;
			}
		else
			{
			Message.println("\tROM size byte is OK") ;
			}

		}
	
	
	/****************************************************************
	* NAME: ValidateCartridgeType (of ROMProcessor)					*
	*																*
	*																*
	****************************************************************/
	
	public void ValidateCartridgeType()
		{
		byte cartType ;
		
		// assert that rom image > minimum possible rom size
		Debug.assert(m_romImage.length >= k_MIN_ROM_SIZE, "ROM Image is smaller than " + k_MIN_ROM_SIZE + " bytes.") ;

		// read current cartridge type
		cartType = m_romImage[k_BYTE_CARTRIDGE_TYPE] ;
		// if actual rom image length > 32kbytes
		if (m_romImage.length>0x8000L)
			{
			// if cartridge type == 0 (an invalid cartridge type for roms > 32kbytes in size)
			if (cartType==0x00)
				{
				// set cartridge type to 1
				m_romImage[k_BYTE_CARTRIDGE_TYPE] = 0x01 ;
				// report that the cartridge type has been changed
				Message.println("\tCartridge type byte changed to 0x01") ;
				}
			else
				{
				// report that the current cartridge type is valid
				Message.println("\tCartridge type byte is OK") ;
				}

			}
		else
			{
			// report that any value cartridge byte is okay for a 32kbyte rom image
			Message.println("\tCartridge type byte is OK") ;
			}

		}

	
	/****************************************************************
	* NAME: getUByte (of ROMProcessor)								*
	*																*
	* Java has absolutely no way to get the unsigned value of a		*
	* negative number. Now is that F*****G stupid, or what? I mean,	*
	* I understand the bugs that can creep in to a program because	*
	* a language supports both signed & unsigned types, but having	*
	* no way to get the unsigned value and having to write a		*
	* function specifically to do it just shows how immature and	*
	* lacking Java, as a language, actually is. Maybe another seven	*
	* to ten years and you can write real programs in it. Until		*
	* then it's just useful for stupid little trick programs like	*
	* this one. But then, it doesn't support enumerated types,		*
	* multiple inheritance, functors, operator overloading, fixed	*
	* memory regions, proper memory allocation functions ("new"		*
	* doesn't count, trust me), or a half-dozen other "useful"		*
	* features that a proper, mature 3GL has (no, Java is not a		*
	* 4GL, don't give me that crap. Proper, mature languages, like, *
	* oh, Fortran, C, C++, Pascal, Delphi, Visual BASIC, Forth,		*
	* ADA. If you cannot access the hardware underneath without		*
	* writing a native class in another language then Java isn't a	*
	* real language, it's just a clever, little, toy language, like	*
	* LOGO. Having said all that, it is useful for cross-platform	*
	* file processing programs, like this one. But then, so is		*
	* PERL.															*
	*																*
	* Somebody pass me a C compiler I need to write a real			*
	* application.													*
	*																*
	****************************************************************/
	
	private short getUByte(byte value)
		{
		// if specified value < 0
		if (value < 0)
			{
			// return two's complement of specified value
			return ((short)(0xFF + value + 1)) ;
			}
		
		// return specified value unchanged
		return (value) ;
		}

	
	/****************************************************************
	* NAME: ChecksumImage (of ROMProcessor)							*
	*																*
	* This function calculates the checksum and the checksum		*
	* complement for a ROM image. It replaces the relevant ROM		*
	* header bytes with the newly calculated results if required.	*
	*																*
	****************************************************************/
	
	public void ChecksumImage()
		{
		short	cartChecksum ;
		short	calculatedChecksum ;
		
		byte	calculatedComplement ;
		byte	cartComplement ;
		
		int	i ;

		// assert that rom image > minimum possible rom size
		Debug.assert(m_romImage.length >= k_MIN_ROM_SIZE, "ROM Image is smaller than " + k_MIN_ROM_SIZE + " bytes.") ;

		// set calculated checksum to zero
		calculatedChecksum = 0 ;
		// set calculated complement to zero
		calculatedComplement = 0 ;
		// read current cartridge checksum
		cartChecksum = (short)((getUByte(m_romImage[k_BYTE_CHECKSUM_MSB])<<8) | getUByte(m_romImage[k_BYTE_CHECKSUM_LSB])) ;
		// read current cartridge complement
		cartComplement = m_romImage[k_BYTE_COMPLEMENT] ;

		Debug.assert(m_romImage.length >= k_MIN_ROM_SIZE, "ROM Image is smaller than " + k_MIN_ROM_SIZE + " bytes.") ;
		// for each byte in rom image
		for(i=0; i<m_romImage.length; i++)
			{
			// get byte from rom image
			short romByte = getUByte(m_romImage[i]) ;
			
			// if byte offset in rom image < rom complement byte or > rom lsb checksum
			if ((i<k_BYTE_COMPLEMENT) || (i>k_BYTE_CHECKSUM_LSB))
				{
				// add value of byte to calculated checksum
				calculatedChecksum += romByte ;
				}
			
			// if byte offset in rom >= start of cartridge title and <= rom version byte
			if ((i>=k_BYTE_CARTRIDGE_TITLE) &&  (i<=k_BYTE_VERSION))
				{
				// add value of byte to calculated complement
				calculatedComplement += romByte ;
				}

			}

		// set final calculated complement
		calculatedComplement = (byte)(0xE7-calculatedComplement) ;
		// add calculated complement to calculated checksum
		calculatedChecksum += calculatedComplement ;

		// if current cartridge checksum != calculated checksum
		if (cartChecksum != calculatedChecksum)
			{
			// set msb of cartridge checksum to msb of calculated checksum
			m_romImage[k_BYTE_CHECKSUM_MSB] = (byte)(calculatedChecksum >> 8) ;
			// set lsb of cartridge checksum to lsb of calculated checksum
			m_romImage[k_BYTE_CHECKSUM_LSB] = (byte)(calculatedChecksum & 0xFF) ;
			// report that current cartridge checksum was invalid and has been changed to new calculated checksum
			Message.println("\tChecksum changed from 0x" + getHex(cartChecksum, 4) + " to 0x" + getHex(calculatedChecksum, 4)) ;
			}
		else
			{
			// report that current cartridge checksum is okay
			Message.println("\tChecksum is OK") ;
			}

		// if current cartridge complement != calculated complement
		if (cartComplement != calculatedComplement)
			{
			// set cartridge complement to calculated complement
			m_romImage[k_BYTE_COMPLEMENT] = (byte)(calculatedComplement) ;
			// report that current cartridge complement was invalid and has been changed to new calculated checksum
			Message.println("\tComplement Checksum changed from 0x" + getHex(cartComplement, 2) + " to 0x" + getHex(calculatedComplement, 2)) ;
			}
		else
			{
			// report that current cartridge complement is okay
			Message.println("\tComplement Checksum is OK") ;
			}

		}

	
	/****************************************************************
	* NAME: ValidateImage (of ROMProcessor)							*
	*																*
	* This function validates all parts of a rom image. It makes	*
	* sure that the Nintendo logo is correct, replacing any invalid	*
	* bytes with the correct values. Validates the ROM size value	*
	* in the ROM image against the actual ROM size. Validates the	*
	* cartridge type and finally calculates the checksum and		*
	* checksum complement.											*
	*																*
	****************************************************************/
	
	public void ValidateImage()
		{
		// report that the rom image is being validated
		Message.println("Validating header:") ;
		// validate & correct nintendo logo
		ValidateNintendoLogo() ;
		// validate & correct rom size
		ValidateROMSize() ;
		// validate & correct cartridge type
		ValidateCartridgeType() ;
		// validate & correct cartridge checksum & complement
		ChecksumImage() ;
		}

	}


