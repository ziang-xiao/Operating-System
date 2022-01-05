package nachos.userprog;

import nachos.machine.*;
import nachos.threads.*;
import nachos.userprog.*;
import nachos.vm.*;

import java.io.EOFException;
import java.util.*;

/**
 * Encapsulates the state of a user process that is not contained in its user
 * thread (or threads). This includes its address translation state, a file
 * table, and information about the program being executed.
 *
 * <p>
 * This class is extended by other classes to support additional functionality
 * (such as additional syscalls).
 *
 * @see nachos.vm.VMProcess
 * @see nachos.network.NetProcess
 */
public class UserProcess {
	/**
	 * Allocate a new process.
	 */
	public UserProcess() {
		int numPhysPages = Machine.processor().getNumPhysPages();
		pageTable = new TranslationEntry[numPhysPages];
		for (int i = 0; i < numPhysPages; i++)
			pageTable[i] = new TranslationEntry(i, i, true, false, false, false);

		for (int i = 2; i < 16; i++) {
			freeFdList.add(i);
		}

		openFiles.put(0, new OpenFile());
		openFiles.put(1, new OpenFile());

		childProcessTableLock = new Lock();
		childProcessTable = new HashMap<UserProcess, ExitStatus>();
	}

	/**
	 * Allocate and return a new process of the correct class. The class name is
	 * specified by the <tt>nachos.conf</tt> key
	 * <tt>Kernel.processClassName</tt>.
	 *
	 * @return a new process of the correct class.
	 */
	public static UserProcess newUserProcess() {
		String name = Machine.getProcessClassName ();

		// If Lib.constructObject is used, it quickly runs out
		// of file descriptors and throws an exception in
		// createClassLoader.  Hack around it by hard-coding
		// creating new processes of the appropriate type.

		if (name.equals ("nachos.userprog.UserProcess")) {
			return new UserProcess ();
		} else if (name.equals ("nachos.vm.VMProcess")) {
			return new VMProcess ();
		} else {
			return (UserProcess) Lib.constructObject(Machine.getProcessClassName());
		}
	}

	/**
	 * Execute the specified program with the specified arguments. Attempts to
	 * load the program, and then forks a thread to run it.
	 *
	 * @param name the name of the file containing the executable.
	 * @param args the arguments to pass to the executable.
	 * @return <tt>true</tt> if the program was successfully executed.
	 */
	public boolean execute(String name, String[] args) {
		if (!load(name, args))
			return false;
		pid = 1; // give pid = 1 to root proc.
		thread = new UThread(this);
		thread.setName(name).fork();

		return true;
	}

	/**
	 * Save the state of this process in preparation for a context switch.
	 * Called by <tt>UThread.saveState()</tt>.
	 */
	public void saveState() {
	}

	/**
	 * Restore the state of this process after a context switch. Called by
	 * <tt>UThread.restoreState()</tt>.
	 */
	public void restoreState() {
		Machine.processor().setPageTable(pageTable);
	}

	/**
	 * Read a null-terminated string from this process's virtual memory. Read at
	 * most <tt>maxLength + 1</tt> bytes from the specified address, search for
	 * the null terminator, and convert it to a <tt>java.lang.String</tt>,
	 * without including the null terminator. If no null terminator is found,
	 * returns <tt>null</tt>.
	 *
	 * @param vaddr the starting virtual address of the null-terminated string.
	 * @param maxLength the maximum number of characters in the string, not
	 * including the null terminator.
	 * @return the string read, or <tt>null</tt> if no null terminator was
	 * found.
	 */
	public String readVirtualMemoryString(int vaddr, int maxLength) {
		Lib.assertTrue(maxLength >= 0);

		byte[] bytes = new byte[maxLength + 1];

		int bytesRead = readVirtualMemory(vaddr, bytes);

		for (int length = 0; length < bytesRead; length++) {
			if (bytes[length] == 0)
				return new String(bytes, 0, length);
		}

		return null;
	}

	/**
	 * Transfer data from this process's virtual memory to all of the specified
	 * array. Same as <tt>readVirtualMemory(vaddr, data, 0, data.length)</tt>.
	 *
	 * @param vaddr the first byte of virtual memory to read.
	 * @param data the array where the data will be stored.
	 * @return the number of bytes successfully transferred.
	 */
	public int readVirtualMemory(int vaddr, byte[] data) {
		return readVirtualMemory(vaddr, data, 0, data.length);
	}

	/**
	 * Transfer data from this process's virtual memory to the specified array.
	 * This method handles address translation details. This method must
	 * <i>not</i> destroy the current process if an error occurs, but instead
	 * should return the number of bytes successfully copied (or zero if no data
	 * could be copied).
	 *
	 * @param vaddr the first byte of virtual memory to read.
	 * @param data the array where the data will be stored.
	 * @param offset the first byte to write in the array.
	 * @param length the number of bytes to transfer from virtual memory to the
	 * array.
	 * @return the number of bytes successfully transferred.
	 */
	public int readVirtualMemory(int vaddr, byte[] data, int offset, int length) {
//		System.out.println("---");
//
//		System.out.println(offset);
//		System.out.println(length);
//		System.out.println(data.length);

		Lib.assertTrue(offset >= 0 && length >= 0
				&& offset + length <= data.length);

		byte[] memory = Machine.processor().getMemory();

//		System.out.println("vaddr:" + Integer.toString(vaddr));
		if (vaddr < 0 || vaddr >= memory.length)
			return 0;

		//*** original impl
//		int amount = Math.min(length, memory.length - vaddr);
//		System.arraycopy(memory, vaddr, data, offset, amount);
//
//		return amount;
		//***

		//*** vm impl
		int addr_itr = vaddr;
		int remaining_bytes = length;
		int finished_bytes = 0;
		while (remaining_bytes > 0) {
			int vpn = Processor.pageFromAddress(addr_itr);
			int vm_offset = Processor.offsetFromAddress(addr_itr);
//			System.out.println(Integer.toString(addr_itr));
			if (vpn < 0 || vpn >= pageTable.length
					|| pageTable[vpn] == null || !pageTable[vpn].valid) {
				return finished_bytes;
			}
			TranslationEntry te = pageTable[vpn];

			int ppn = te.ppn;
			int amount = Math.min(remaining_bytes, pageSize - vm_offset);

			int phys_addr = ppn * pageSize + vm_offset;
			System.arraycopy(memory, phys_addr, data, offset + finished_bytes, amount);
			finished_bytes += amount;
			remaining_bytes -= amount;
			addr_itr += amount;
		}

		return finished_bytes;
		//***
	}

	/**
	 * Transfer all data from the specified array to this process's virtual
	 * memory. Same as <tt>writeVirtualMemory(vaddr, data, 0, data.length)</tt>.
	 *
	 * @param vaddr the first byte of virtual memory to write.
	 * @param data the array containing the data to transfer.
	 * @return the number of bytes successfully transferred.
	 */
	public int writeVirtualMemory(int vaddr, byte[] data) {
		return writeVirtualMemory(vaddr, data, 0, data.length);
	}

	/**
	 * Transfer data from the specified array to this process's virtual memory.
	 * This method handles address translation details. This method must
	 * <i>not</i> destroy the current process if an error occurs, but instead
	 * should return the number of bytes successfully copied (or zero if no data
	 * could be copied).
	 *
	 * @param vaddr the first byte of virtual memory to write.
	 * @param data the array containing the data to transfer.
	 * @param offset the first byte to transfer from the array.
	 * @param length the number of bytes to transfer from the array to virtual
	 * memory.
	 * @return the number of bytes successfully transferred.
	 */
	public int writeVirtualMemory(int vaddr, byte[] data, int offset, int length) {
		Lib.assertTrue(offset >= 0 && length >= 0
				&& offset + length <= data.length);

		byte[] memory = Machine.processor().getMemory();

		if (vaddr < 0 || vaddr >= memory.length)
			return 0;

		//*** original impl
//
//		int amount = Math.min(length, memory.length - vaddr);
//		System.arraycopy(data, offset, memory, vaddr, amount);
//
//		return amount;
		//***

		//*** vm impl

		int addr_itr = vaddr;
		int remaining_bytes = length;
		int finished_bytes = 0;
		while (remaining_bytes > 0) {
			int vpn = Processor.pageFromAddress(addr_itr);
			int vm_offset = Processor.offsetFromAddress(addr_itr);
//			System.out.println("vaddr = " + addr_itr);
//			System.out.println("vpn = " + vpn);
//			System.out.println("offset = " + vm_offset);

//			System.out.println(Integer.toString(addr_itr));
			if (vpn < 0 || vpn >= pageTable.length
					|| pageTable[vpn] == null || !pageTable[vpn].valid || pageTable[vpn].readOnly) {
				return finished_bytes;
			}
			TranslationEntry te = pageTable[vpn];

			int ppn = te.ppn;
			int amount = Math.min(remaining_bytes, pageSize - vm_offset);

			int phys_addr = ppn * pageSize + vm_offset;
			System.arraycopy(data, offset + finished_bytes, memory, phys_addr, amount);
			finished_bytes += amount;
			remaining_bytes -= amount;
			addr_itr += amount;
		}

		return finished_bytes;
		//***
	}

	/**
	 * Load the executable with the specified name into this process, and
	 * prepare to pass it the specified arguments. Opens the executable, reads
	 * its header information, and copies sections and arguments into this
	 * process's virtual memory.
	 *
	 * @param name the name of the file containing the executable.
	 * @param args the arguments to pass to the executable.
	 * @return <tt>true</tt> if the executable was successfully loaded.
	 */
	private boolean load(String name, String[] args) {
		Lib.debug(dbgProcess, "UserProcess.load(\"" + name + "\")");

		OpenFile executable = ThreadedKernel.fileSystem.open(name, false);
		if (executable == null) {
			Lib.debug(dbgProcess, "\topen failed");
			return false;
		}

		try {
			coff = new Coff(executable);
		}
		catch (EOFException e) {
			executable.close();
			Lib.debug(dbgProcess, "\tcoff load failed");
			return false;
		}

		// make sure the sections are contiguous and start at page 0
		numPages = 0;
		for (int s = 0; s < coff.getNumSections(); s++) {
			CoffSection section = coff.getSection(s);
			if (section.getFirstVPN() != numPages) {
				coff.close();
				Lib.debug(dbgProcess, "\tfragmented executable");
				return false;
			}
			numPages += section.getLength();
		}


		// make sure the argv array will fit in one page
		byte[][] argv = new byte[args.length][];
		int argsSize = 0;
		for (int i = 0; i < args.length; i++) {
			argv[i] = args[i].getBytes();
			// 4 bytes for argv[] pointer; then string plus one for null byte
			argsSize += 4 + argv[i].length + 1;
		}
		if (argsSize > pageSize) {
			coff.close();
			Lib.debug(dbgProcess, "\targuments too long");
			return false;
		}

		// program counter initially points at the program entry point
		initialPC = coff.getEntryPoint();

		// next comes the stack; stack pointer initially points to top of it
		numPages += stackPages;
		initialSP = numPages * pageSize;

		// and finally reserve 1 page for arguments
		numPages++;

		if (!loadSections())
			return false;

		// store arguments in last page
		int entryOffset = (numPages - 1) * pageSize;
		int stringOffset = entryOffset + args.length * 4;

		this.argc = args.length;
		this.argv = entryOffset;

		for (int i = 0; i < argv.length; i++) {
			byte[] stringOffsetBytes = Lib.bytesFromInt(stringOffset);
			Lib.assertTrue(writeVirtualMemory(entryOffset, stringOffsetBytes) == 4);
			entryOffset += 4;
			Lib.assertTrue(writeVirtualMemory(stringOffset, argv[i]) == argv[i].length);
			stringOffset += argv[i].length;
			Lib.assertTrue(writeVirtualMemory(stringOffset, new byte[] { 0 }) == 1);
			stringOffset += 1;
		}

		return true;
	}

	/**
	 * Allocates memory for this process, and loads the COFF sections into
	 * memory. If this returns successfully, the process will definitely be run
	 * (this is the last step in process initialization that can fail).
	 *
	 * @return <tt>true</tt> if the sections were successfully loaded.
	 */
	protected boolean loadSections() {

		if (numPages > Machine.processor().getNumPhysPages()) {
			coff.close();
			Lib.debug(dbgProcess, "\tinsufficient physical memory");
			return false;
		}
		UserKernel.physPageListLock.acquire();
		if (UserKernel.freePhysicalPages.size() < numPages) {
			// available page not enough
			UserKernel.physPageListLock.release();
			return false;
		}
//		System.out.printf("allocated %d pages, remaining: %d\n", numPages,UserKernel.freePhysicalPages.size() - numPages );
		// load sections
		for (int s = 0; s < coff.getNumSections(); s++) {
			CoffSection section = coff.getSection(s);
//			System.out.println(section.getName());
//
//			System.out.println(section.getLength());
//			System.out.println(section.getFirstVPN());

			Lib.debug(dbgProcess, "\tinitializing " + section.getName()
					+ " section (" + section.getLength() + " pages)");

			for (int i = 0; i < section.getLength(); i++) {
				int vpn = section.getFirstVPN() + i;
//				System.out.println("finding a page for vpn:" + Integer.toString(vpn));
				// *** original impl.
				// for now, just assume virtual addresses=physical addresses
//				section.loadPage(i, vpn);
				// ***

				// *** vm impl.
				// first, find a free physical page.
				int ppn = UserKernel.freePhysicalPages.removeFirst();

				Lib.assertTrue(vpn >= 0 && vpn < pageTable.length);
				TranslationEntry te = pageTable[vpn];
				te.valid = true;
				te.ppn = ppn;
				te.readOnly = section.isReadOnly();
//				System.out.println( Integer.toString(vpn) + "is mapped to ppn:" + Integer.toString(te.ppn));
				section.loadPage(i, ppn);
				//***
			}
		}
		// allocate stck and args
		for (int i = 1 + stackPages; i >= 1; i--) {
			int vpn = numPages - i;
			int ppn = UserKernel.freePhysicalPages.removeFirst();
			TranslationEntry te = pageTable[vpn];

			te.valid = true;
			te.ppn = ppn;
//			System.out.println( Integer.toString(vpn) + "is mapped to ppn:" + Integer.toString(te.ppn));

		}
		UserKernel.physPageListLock.release();

		return true;
	}

	/**
	 * Release any resources allocated by <tt>loadSections()</tt>.
	 */
	protected void unloadSections() {

		for (int s = 0; s < coff.getNumSections(); s++) {
			CoffSection section = coff.getSection(s);

			Lib.debug(dbgProcess, "\tdeleting " + section.getName()
					+ " section (" + section.getLength() + " pages)");

			for (int i = 0; i < section.getLength(); i++) {
				int vpn = section.getFirstVPN() + i;

				UserKernel.physPageListLock.acquire();
				Lib.assertTrue(vpn >= 0 && vpn < pageTable.length
						&& pageTable[vpn] != null && pageTable[vpn].valid);
				TranslationEntry te = pageTable[vpn];

				te.valid = false;
				int ppn = te.ppn;
				UserKernel.freePhysicalPages.add(ppn);
				UserKernel.physPageListLock.release();

//				System.out.println( Integer.toString(vpn) + "'s physical page number" + Integer.toString(ppn) + "is released");
				// section.loadPage(i, ppn);
			}
		}

		// delete stck and args
		for (int i = 1 + stackPages; i >= 1; i--) {
			int vpn = numPages - i;

			UserKernel.physPageListLock.acquire();
			Lib.assertTrue(vpn >= 0 && vpn < pageTable.length
					&& pageTable[vpn] != null && pageTable[vpn].valid);
			TranslationEntry te = pageTable[vpn];

			te.valid = false;
			int ppn = te.ppn;
			UserKernel.freePhysicalPages.add(ppn);
			UserKernel.physPageListLock.release();

//			System.out.println( Integer.toString(vpn) + "'s physical page number" + Integer.toString(ppn) + "is released");
		}
	}


	/**
	 * Initialize the processor's registers in preparation for running the
	 * program loaded into this process. Set the PC register to point at the
	 * start function, set the stack pointer register to point at the top of the
	 * stack, set the A0 and A1 registers to argc and argv, respectively, and
	 * initialize all other registers to 0.
	 */
	public void initRegisters() {
		Processor processor = Machine.processor();

		// by default, everything's 0
		for (int i = 0; i < processor.numUserRegisters; i++)
			processor.writeRegister(i, 0);

		// initialize PC and SP according
		processor.writeRegister(Processor.regPC, initialPC);
		processor.writeRegister(Processor.regSP, initialSP);

		// initialize the first two argument registers to argc and argv
		processor.writeRegister(Processor.regA0, argc);
		processor.writeRegister(Processor.regA1, argv);
	}

	/**
	 * Handle the halt() system call.
	 */
	private int handleHalt() {

		if (pid != 1) {
			// if not root proc, return immediately.
			return -1;
		}
		Machine.halt();

		Lib.assertNotReached("Machine.halt() did not halt machine!");
		return 0;
	}

	/**
	 * Handle unexpected exception.
	 */
	private void handleUnexpectedException() {
		unloadSections();
		//close fd
		Set<Integer> fdSet = new HashSet<>(openFiles.keySet());
		for (int fd : fdSet) {
			handleClose(fd);
		}

//		System.out.println("exiting abnormally");
		if (parent != null) {
			childProcessTableLock.acquire();
			Lib.assertTrue(parent.childProcessTable.containsKey(this));
			parent.childProcessTable.put(this, new ExitStatus(-1, false));
			childProcessTableLock.release();
		}

		//farewell to its children
		Set<UserProcess> childSet = new HashSet<>(childProcessTable.keySet());
		for (UserProcess child : childSet) {
			child.parent = null;
		}

		coff.close();

		UserKernel.userProcessCountLock.acquire();
		UserKernel.userProcessCount --;

		if (UserKernel.userProcessCount == 0) {
			UserKernel.userProcessCountLock.release();
			Kernel.kernel.terminate();
		}
		else {
			UserKernel.userProcessCountLock.release();
			KThread.finish();
		}
	}

	/**
	 * Handle the exit() system call.
	 */
	private int handleExit(int status) {
		// Do not remove this call to the autoGrader...
		Machine.autoGrader().finishingCurrentProcess(status);
		// ...and leave it as the top of handleExit so that we
		// can grade your implementation.

		// *** original impl
//		Lib.debug(dbgProcess, "UserProcess.handleExit (" + status + ")");
//		// for now, unconditionally terminate with just one process
//		Kernel.kernel.terminate();
		// ***

		unloadSections();
		//close fd
		Set<Integer> fdSet = new HashSet<>(openFiles.keySet());
		for (int fd : fdSet) {
			handleClose(fd);
		}

		//farewell to its children
		Set<UserProcess> childSet = new HashSet<>(childProcessTable.keySet());
		for (UserProcess child : childSet) {
			child.parent = null;
		}
//		System.out.println("exiting with status" + status);
		if (parent != null) {
			childProcessTableLock.acquire();
			Lib.assertTrue(parent.childProcessTable.containsKey(this));
			parent.childProcessTable.put(this, new ExitStatus(status, true));
			childProcessTableLock.release();
		}

		coff.close();

		UserKernel.userProcessCountLock.acquire();
		UserKernel.userProcessCount --;

		if (UserKernel.userProcessCount == 0) {
			UserKernel.userProcessCountLock.release();
			Kernel.kernel.terminate();
		}
		else {
			UserKernel.userProcessCountLock.release();
			KThread.finish();
		}

		return 0;
	}

	/**
	 * Handle the exec() system call.
	 * int exec(char *file, int argc, char *argv[]);
	 */
	private int handleExec(int name, int argc, int argv) {
		if (argc < 0) return -1;
		String[] args = new String[argc];
		String fileName = readVirtualMemoryString(name, 256);
		if (fileName == null) {
			return -1;
		}

		byte[] addrs = new byte[4 * argc];
		if (readVirtualMemory(argv, addrs) < addrs.length) {
			return -1;
		}

		for (int i = 0; i < argc; i++) {
			int addr = Lib.bytesToInt(addrs, i * 4, 4);
			args[i] = readVirtualMemoryString(addr, 256);
			if (args[i] == null) {
				return -1;
			}
		}

		UserProcess child = new UserProcess();
		if (!child.load(fileName, args))  {
//			System.out.println("load failed.");
			return -1;

		}
		
		// gen a new pid.
		UserKernel.pidCounterLock.acquire();
		child.pid = UserKernel.pidCounter;
		UserKernel.pidCounter ++;
		UserKernel.pid2UserProcess.put(child.pid, child);
		UserKernel.pidCounterLock.release();

		childProcessTableLock.acquire();
		childProcessTable.put(child, null);
		childProcessTableLock.release();

		UserKernel.userProcessCountLock.acquire();
		UserKernel.userProcessCount ++;
		UserKernel.userProcessCountLock.release();
		
		child.parent = this;
		child.thread = new UThread(child);
		child.thread.setName(fileName).fork();
		
		return child.pid;
	}

	/**
	 * Handle the join() system call.
	 * int join(int processID, int *status);
	 */
	private int handleJoin(int pid, int status) {

//		System.out.println("handle join param: status=" + status);

		// write a dummy int to status, to check if the addr is valid.
		if (status != 0 && writeVirtualMemory(status, Lib.bytesFromInt(0)) != 4) {
			return -1;
		}

		UserProcess child = UserKernel.pid2UserProcess.get(pid);
//		System.out.println(childProcessTableSet);
//		System.out.println(UserKernel.pid2UserProcess);

		if (child == null || !childProcessTable.containsKey(child)) {
			// process not found or not a child
//			System.out.println("pid:" + Integer.toString(pid) + "not found!");
//			System.out.println();
			return -1;
		}

		// join the child's thread;
		// this will put this parent process to sleep if child proc is still running.
		child.thread.join();

		Lib.assertTrue(this.childProcessTable.containsKey(child) && this.childProcessTable.get(child) != null);
		ExitStatus es = this.childProcessTable.get(child);
		// if status is not null
		if (status > 0 && es.normalExit) {
//			System.out.println("writing " + Integer.toString(es.status) + "to:" + Integer.toString(status));
			Lib.assertTrue (writeVirtualMemory(status, Lib.bytesFromInt(es.status)) == 4);
		}

		// disowns the child proc, so that join can not be performed again.
		childProcessTable.remove(child);
		return es.normalExit ? 1 : 0;
	}

	/**
	 * Handle the open() system call.
	 */
	private int handleOpen(int name) {
		String fileName = readVirtualMemoryString(name, 256);
		if (fileName == null) {
//			System.out.println("no filename");
			return -1;
		}
		OpenFile of = ThreadedKernel.fileSystem.open(fileName, false);

		if (of == null) {
//			System.out.println("file system open failed!");
			return -1;
		}
		if (freeFdList.isEmpty()) {
			// no available fd.
			of.close();
//			System.out.println("no available fd!");
			return -1;
		}
		int fd = freeFdList.removeFirst();

		openFiles.put(fd, of);
		return fd;
	}

	/**
	 * Handle the create() system call.
	 */
	private int handleCreate(int name) {
		if (freeFdList.isEmpty()) {
			// no available fd.
			return -1;
		}
		String fileName = readVirtualMemoryString(name, 256);
		if (fileName == null) {
			return -1;
		}
		OpenFile of = ThreadedKernel.fileSystem.open(fileName, true);
		if (of == null) {
			return -1;
		}

		int fd = freeFdList.removeFirst();
		openFiles.put(fd, of);
		return fd;
	}

	/**
	 * Handle the close() system call.
	 */
	private int handleClose(int fd) {
		OpenFile of = openFiles.get(fd);
		if (of == null) {
			return -1;
		}
		if(fd == 0) {
			hasStdin = false;
		}
		if(fd == 1) {
			hasStdout = false;
		}
		of.close();
		openFiles.remove(fd);
		freeFdList.add(fd);
		return 0;
	}

	/**
	 * Handle the unlink() system call.
	 */
	private int handleUnlink(int name) {
		String fileName = readVirtualMemoryString(name, 256);
		if (fileName == null) {
			return -1;
		}
		if (!ThreadedKernel.fileSystem.remove(fileName)) {
			return -1;
		}
		return 0;
	}

	/**
	 * Handle the read() system call.
	 */
	private int handleRead(int fd, int buffer, int size) {
		if (size < 0) return -1;
		OpenFile of;
		if (fd == 0 && this.hasStdin) {
			of = UserKernel.console.openForReading();
		}
		else {
			of = openFiles.get(fd);
		}
		if (of == null) {
			return -1;
		}
		if (size == 0) {
			return 0;
		}
		byte[] buf = new byte[pageSize];
//		System.out.println("fd = " + fd);
		int remainingByteSize = size, finishedSum = 0;
		while (remainingByteSize > 0) {
//			System.out.println("remaining = " + remainingByteSize);
			int readSize = of.read(buf, 0, Math.min(pageSize, remainingByteSize));
			if (readSize < 0) {
				return -1;
			}
//			System.out.println("read from file = " + readSize + " bytes");
			remainingByteSize -= readSize;
			int ret = writeVirtualMemory(buffer+finishedSum, buf, 0, readSize);
			if (ret < readSize) {
				// one possible reason: buffer is invalid.
				return -1;
			}
			finishedSum += readSize;

			// consider if `size` is greater than filesize. without early break it will be an endless loop.
			if (readSize < pageSize) {
				break;
			}
		}
		return finishedSum;
	}

	/**
	 * Handle the write() system call.
	 */
	private int handleWrite(int fd, int buffer, int size) {
		if (size < 0) return -1;
		OpenFile of;
		if (fd == 1 && this.hasStdout) {
			of = UserKernel.console.openForWriting();
		}
		else {
			of = openFiles.get(fd);
		}
		if (of == null) {
			return -1;
		}
		byte[] buf = new byte[pageSize];
		int remainingByteSize = size, finishedSum = 0;

		while (remainingByteSize > 0) {
			int readSize = Math.min(pageSize, remainingByteSize);
			int ret = readVirtualMemory(buffer+finishedSum, buf, 0, readSize);
			if (ret < readSize) {
				return -1;
			}
			int successfulReadSize = of.write(buf, 0, readSize);
			if (successfulReadSize < 0) {
				return -1;
			}
			finishedSum += successfulReadSize;
			remainingByteSize -= successfulReadSize;
			if (successfulReadSize < readSize) {
				break;
			}
		}
		if (finishedSum < size) {
			return -1;
		}
		return finishedSum;
	}

	private static final int syscallHalt = 0, syscallExit = 1, syscallExec = 2,
			syscallJoin = 3, syscallCreate = 4, syscallOpen = 5,
			syscallRead = 6, syscallWrite = 7, syscallClose = 8,
			syscallUnlink = 9;

	/**
	 * Handle a syscall exception. Called by <tt>handleException()</tt>. The
	 * <i>syscall</i> argument identifies which syscall the user executed:
	 *
	 * <table>
	 * <tr>
	 * <td>syscall#</td>
	 * <td>syscall prototype</td>
	 * </tr>
	 * <tr>
	 * <td>0</td>
	 * <td><tt>void halt();</tt></td>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td><tt>void exit(int status);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>2</td>
	 * <td><tt>int  exec(char *name, int argc, char **argv);
	 * 								</tt></td>
	 * </tr>
	 * <tr>
	 * <td>3</td>
	 * <td><tt>int  join(int pid, int *status);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>4</td>
	 * <td><tt>int  creat(char *name);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>5</td>
	 * <td><tt>int  open(char *name);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>6</td>
	 * <td><tt>int  read(int fd, char *buffer, int size);
	 * 								</tt></td>
	 * </tr>
	 * <tr>
	 * <td>7</td>
	 * <td><tt>int  write(int fd, char *buffer, int size);
	 * 								</tt></td>
	 * </tr>
	 * <tr>
	 * <td>8</td>
	 * <td><tt>int  close(int fd);</tt></td>
	 * </tr>
	 * <tr>
	 * <td>9</td>
	 * <td><tt>int  unlink(char *name);</tt></td>
	 * </tr>
	 * </table>
	 *
	 * @param syscall the syscall number.
	 * @param a0 the first syscall argument.
	 * @param a1 the second syscall argument.
	 * @param a2 the third syscall argument.
	 * @param a3 the fourth syscall argument.
	 * @return the value to be returned to the user.
	 */
	public int handleSyscall(int syscall, int a0, int a1, int a2, int a3) {
		switch (syscall) {
			case syscallHalt:
				return handleHalt();
			case syscallExit:
				return handleExit(a0);
			case syscallCreate:
				return handleCreate(a0);
			case syscallRead:
				return handleRead(a0, a1, a2);
			case syscallWrite:
				return handleWrite(a0, a1, a2);
			case syscallOpen:
				return handleOpen(a0);
			case syscallClose:
				return handleClose(a0);
			case syscallUnlink:
				return handleUnlink(a0);
			case syscallExec:
				return handleExec(a0, a1, a2);
			case syscallJoin:
				return handleJoin(a0, a1);

			default:
				Lib.debug(dbgProcess, "Unknown syscall " + syscall);
				Lib.assertNotReached("Unknown system call!");
		}
		return 0;
	}

	/**
	 * Handle a user exception. Called by <tt>UserKernel.exceptionHandler()</tt>
	 * . The <i>cause</i> argument identifies which exception occurred; see the
	 * <tt>Processor.exceptionZZZ</tt> constants.
	 *
	 * @param cause the user exception that occurred.
	 */
	public void handleException(int cause) {
		Processor processor = Machine.processor();

		switch (cause) {
			case Processor.exceptionSyscall:
				int result = handleSyscall(processor.readRegister(Processor.regV0),
						processor.readRegister(Processor.regA0),
						processor.readRegister(Processor.regA1),
						processor.readRegister(Processor.regA2),
						processor.readRegister(Processor.regA3));
				processor.writeRegister(Processor.regV0, result);
				processor.advancePC();
				break;

			default:
				handleUnexpectedException();
				Lib.debug(dbgProcess, "Unexpected exception: "
						+ Processor.exceptionNames[cause]);
				Lib.assertNotReached("Unexpected exception");
		}
	}

	public static class ExitStatus {
		ExitStatus(int s, boolean ne) {
			this.status = s;
			this.normalExit = ne;
		}
		public int status;
		public boolean normalExit;

	}
	/** */
	private int pid;

	private HashMap<Integer, OpenFile> openFiles = new HashMap<>();

	private LinkedList<Integer> freeFdList = new LinkedList<>();

	private boolean hasStdin = true;
	private boolean hasStdout = true;

	public Lock childProcessTableLock;
	public HashMap<UserProcess, ExitStatus> childProcessTable;

	private UserProcess parent;

	/** The program being run by this process. */
	protected Coff coff;

	/** This process's page table. */
	protected TranslationEntry[] pageTable;

	/** The number of contiguous pages occupied by the program. */
	protected int numPages;

	/** The number of pages in the program's stack. */
	protected final int stackPages = 8;

	/** The thread that executes the user-level program. */
	protected UThread thread;

	private int initialPC, initialSP;

	private int argc, argv;

	private static final int pageSize = Processor.pageSize;

	private static final char dbgProcess = 'a';
}


