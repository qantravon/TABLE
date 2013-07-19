#include <device.h>
#include <stdio.h>

void storeAndCalculateAverages();
void sendValues();

unsigned int averages[8][8];

void initAvgs() //initialize averages array
{
    int i = 0;
    int j = 0;
    for(i = 0; i < 8; ++i)
    {
        for(j = 0; j < 8; ++j)
        {
            averages[i][j] = 0;
        }
    }
}

void main()
{
	CYGlobalIntEnable; //enable intterupts
	
	UART_Start();
	LCD_Char_1_Start();
	LCD_Char_1_ClearDisplay();
   
    int counter = 0;
    
	while(1)
	{
		if(UART_ReadRxStatus() == UART_RX_STS_FIFO_NOTEMPTY)	//if the buffer has data available
		{
			initAvgs();					//Initializes the averages array
            LCD_Char_1_Position(0,14);	//Shows that it is processing and writing
			LCD_Char_1_PutChar('R');
			LCD_Char_1_Position(1,0);
			LCD_Char_1_PrintString("Processing");
            
			storeAndCalculateAverages();
            UART_ClearRxBuffer();
			
            LCD_Char_1_Position(0,14);
			LCD_Char_1_PutChar('W');
			LCD_Char_1_Position(0,0);
			LCD_Char_1_PrintString("   ");
			UART_ClearTxBuffer();
            sendValues();
			UART_ClearTxBuffer();
			LCD_Char_1_ClearDisplay();
            counter = 0;
		}
		else if(counter > 5000)
        {
            CySoftwareReset();
        }
        else
            counter++;
	}
}

void storeAndCalculateAverages() 
{
	int j;
	int i;
    uint8 readchar;

    //storing the sums of each pixel in 60x40 square
    LCD_Char_1_Position(0,15);
	LCD_Char_1_PrintNumber(1);
	for(j = 0; j < 120; j++)		//each row
	{
		for(i = 0; i < 140; i++)	//each column
		{
            if((UART_ReadRxStatus() == UART_RX_STS_OVERRUN) || (UART_ReadRxStatus() == UART_RX_STS_SOFT_BUFF_OVER))
            {	//Checks for buffer overflow, freezes program if necessary. 
                while(1)
                {
                    LCD_Char_1_ClearDisplay();
            		LCD_Char_1_PrintString("Overflow");
                }
            }
            if(UART_ReadRxStatus() == UART_RX_STS_FIFO_NOTEMPTY)	//if the buffer has data available:
		    {
                LCD_Char_1_Position(0,11);
                LCD_Char_1_PutChar('T');
            }
            else
            {
                LCD_Char_1_Position(0,11);
                LCD_Char_1_PutChar('F');
            }
            readchar = UART_GetChar();			//gets char from serial buffer (RX)
			averages[j/15][i/17] += readchar;	//computes the sum for each square in the 8x8 2D array.
        }
	}
	UART_ClearRxBuffer();						//clears all data from the buffer.
	LCD_Char_1_Position(0,15);
	LCD_Char_1_PrintNumber(2);
    
    for(j = 0; j < 8; j++)				//each row
	{
		for(i = 0; i < 8; i++)			//each column
		{
			averages[j][i] /= 300;		//computes average of each square
		}
	}
}

void sendValues()
{
	int j;
	int i;
    uint8 sendArray[64];		//array of 64 truth values for each square in the grid
	for(j = 0; j < 8; j++)		//each row
	{
		for(i = 0; i < 8; i++)	//each column
		{
            if(averages[j][i] > 10)			//if the average blue value of the square is above a threshhold 
			{
				sendArray[j+(i*8)] = 1;		//send back a value indicating this square should be lit
			}
			else
			{
				sendArray[j+(i*8)] = 0;		//else, no piece present (do not light square)
			}
		}
	}
    UART_PutArray(sendArray,64);					//transmit array through serial (TX)
    if(UART_ReadTxStatus() == UART_TX_STS_COMPLETE)	
    {
        LCD_Char_1_Position(0,0);
        LCD_Char_1_PrintString("Complete!");
    }
    else
    {
        LCD_Char_1_Position(0,0);
        LCD_Char_1_PrintString("Failed!");
    }
}
/* [] END OF FILE */