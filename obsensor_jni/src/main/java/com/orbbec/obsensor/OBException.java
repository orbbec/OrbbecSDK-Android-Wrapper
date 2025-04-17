package com.orbbec.obsensor;

/**
 * \if English
 * Exception thrown by Orbbec sdk
 * \else
 * Orbbec sdk抛出的异常
 * \endif
 */
public class OBException extends RuntimeException {
    /**
	 * \if English
	 * Create Orbbec SDK exception
	 * \else
     * 创建Orbbec SDK异常
	 * \endif
     */
    public OBException() {
        super();
    }

    /**
	 * \if English
	 * Create Orbbec SDK exception with specific information
     *
     * @param message details
	 * \else
     * 通过具体信息创建Orbbec SDK异常
     *
     * @param message 详细信息
	 * \endif
     */
    public OBException(String message) {
        super(message);
    }
}
