// name1- Noy Netanel
// name2- Ofir Strull

/**
 * BinomialHeap
 * <p>
 * An implementation of binomial heap over non-negative integers.
 * Based on exercise from previous semester.
 */
public class BinomialHeap {

    /**
     * empty constructor
     */
    public BinomialHeap() {

    }

    /**
     * build a binomial heap from the given node
     */
    private BinomialHeap(HeapNode first) {
        this.min = first;
        this.size = 1;
        this.last = first;
        this.numTrees = 1;
    }
    /**
     * given an array of roots, create a new binomial heap
     * @param size - the size of the new binomial heap
     * run time complexity is O(logn)- for loop
     */
    private BinomialHeap(HeapNode[] roots, int size) {
        min = roots[(roots.length - 1)];
        for (int i = 0; i < roots.length - 1; i++) {
            if (roots[i].item.key < min.item.key) {
                min = roots[i];   // update the minimum
            }
            roots[i].next = roots[i + 1];
        }
        roots[roots.length - 1].next = roots[0];
        this.last = roots[roots.length - 1];
        this.size = size;
        this.numTrees = roots.length;
    }

    private int numTrees;
    public int size;
    public HeapNode last;
    public HeapNode min;

    /**
     * @param node- a given node
     * @return a new binomial heap made of this node
     */
    public static BinomialHeap getSingleNodeHeap(HeapNode node) {
        return new BinomialHeap(node);
    }

    /**
     * return the index (at the array) of the given object
     * @return the index at the array,-1 if doesn't exist
     * run time complexity is O(logn)
     */
    private int indexOf(Object[] array, Object object) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == object)
                return i;
        }
        return -1;
    }
    /**
     * @return a list of roots for the current heap, using the binary represanation of the size.
     * time complexity- O(logn)
     */
    public HeapNode[] getRoots() {
        String binaryRep = Integer.toBinaryString(size);
        HeapNode[] roots = new HeapNode[binaryRep.length()];
        HeapNode current = last.next;
        for (int i = binaryRep.length() - 1; i >= 0; i--) {
            if (binaryRep.charAt(i) == '1') {    // if we see 1 then there should be a root at the index
                roots[binaryRep.length() - 1 - i] = current;
                current = current.next;
            }
        }
        return roots;
    }


    /**
     * Merges to subtrees by their roots, and returns the new root
     * run time complexity is O(1)
     */
    private static HeapNode link(HeapNode root1, HeapNode root2, HeapNode minIfEquals) {
        HeapNode minRoot;
        if (minIfEquals != null)
            minRoot = minIfEquals;
        else
            minRoot = root1.item.key < root2.item.key ? root1 : root2;
        HeapNode notMinRoot = minRoot == root1 ? root2 : root1;

        HeapNode minOldChild = minRoot.child;
        // change the first child and parent
        minRoot.child = notMinRoot;
        notMinRoot.parent = minRoot;

        minRoot.rank++;

        if (minOldChild != null) {
            notMinRoot.next = minOldChild.next;
            minOldChild.next = notMinRoot;
        } else {
            notMinRoot.next = notMinRoot;
        }

        return minRoot;
    }

    /**
     * pre: key > 0
     * <p>
     * Insert (key,info) into the heap and return the newly generated HeapItem.
     * run time complexity is O(logn)- using meld
     */
    public HeapItem insert(int key, String info) {
        // first case - heap was empty
        if (last == null) {
            HeapNode first = new HeapNode(new HeapItem(key, info));
            first.item.node = first;
            this.min = first;
            this.size = 1;
            this.last = first;
            this.numTrees = 1;
            return first.item;
        }

        HeapNode inserted = new HeapNode(new HeapItem(key, info));
        inserted.item.node = inserted;

        // if the size is even:
        if (size % 2 == 0) {
            inserted.next = last.next;
            last.next = inserted;
            if (inserted.item.key < min.item.key)
                min = inserted;
            size++;
            numTrees++;
        } else {
            BinomialHeap singleNodeHeap = getSingleNodeHeap(inserted);
            // use meld with a heap made of single node
            meld(singleNodeHeap);
        }
        return inserted.item;
    }

    /**
     * Delete the minimal item
     * run time complexity is O(logn)
     */
    public void deleteMin() {
        if (size <= 1) {
            min = null;
            last = null;
            size = 0;
            numTrees = 0;
            return;
        }

        double subHeapSize = Math.pow(2, indexOf(getRoots(), min)) - 1;
        this.size -= (subHeapSize + 1);

        // ------------- Detach the min -----------------
        // set the next pointers to skip the minimum
        HeapNode currentRoot = min.next;
        HeapNode newMin = currentRoot;
        HeapNode beforeMin = null;
        while (currentRoot != min) {
            // find the before min for later
            if (currentRoot.next == min)
                beforeMin = currentRoot;
            // find the new min
            if (newMin.item.key > currentRoot.item.key)
                newMin = currentRoot;
            currentRoot = currentRoot.next;
        }
        // if there is only one tree, there is nothing before the minimum
        if (numTrees != 1)
            beforeMin.next = min.next;
        // if we deleted the last, we need to update it
        if (last == min)
            last = beforeMin;
        // ---------------------------------------
        // ------------- Create sub heap  -----------------
        // create list of subRoots to the children of min
        if (min.child == null) {
            this.min = newMin;
            this.numTrees--;
            return;
        }
        HeapNode[] subRoots = new HeapNode[min.rank];
        HeapNode currentSubRoot = min.child.next;
        int index = 0;
        do {
            currentSubRoot.parent = null;
            subRoots[index] = currentSubRoot;
            currentSubRoot = currentSubRoot.next;
            index++;
        }
        while (currentSubRoot != min.child.next);
        BinomialHeap subHeap = new BinomialHeap(subRoots, (int) subHeapSize);
        // ---------------------------
        if (numTrees == 1) {
            this.min = subHeap.min;
            this.last = subHeap.last;
            this.size = subHeap.size;
            this.numTrees = subHeap.numTrees;
        } else {
            this.min = newMin;
            meld(subHeap);
        }
    }


    /**
     * Return the minimal HeapItem
     * run time complexity is O(1)
     */
    public HeapItem findMin() {
        if (min == null)
            return null;
        return min.item;
    }

    /**
     * pre: 0<diff<item.key
     * <p>
     * Decrease the key of item by diff and fix the heap.
     * run time complexity is O(logn) - using heapify up
     */
    public void decreaseKey(HeapItem item, int diff) {
        item.key -= diff;
        heapifyUp(item.node);
        if (item.key < min.item.key)
            min = item.node;
    }
    /**
     * recursive heapify up, just like we saw in class
     * keeps switching parent with node until we keep the heap rule
     * run time complexity is O(logn) wc
     */
    private void heapifyUp(HeapNode node) {
        HeapNode parent = node.parent;
        if (parent == null)
            return;
        HeapItem parentItem = parent.item;
        // make sure we keep the heap rule.
        if (node.item.key < parent.item.key) {
            parent.item = node.item;
            parent.item.node = parent;

            node.item = parentItem;
            node.item.node = node;

            heapifyUp(parent);
        }
    }

    /**
     * Delete the item from the heap.
     * run time complexity is O(logn) - using delete min
     */
    public void delete(HeapItem item) {
        // decrease key to -infinity and then delete the min
        decreaseKey(item, Integer.MIN_VALUE);
        min = item.node;
        deleteMin();
    }

    /**
     * returns a list of the non nulls nodes from the given 3 nodes
     * runtime complexity is O(1)
     */

    public HeapNode[] getNonNulls(HeapNode a, HeapNode b, HeapNode c) {
        int count = 0;
        // find the non nulls
        if (a != null)
            count++;
        if (b != null)
            count++;
        if (c != null)
            count++;
        HeapNode[] nonNulls = new HeapNode[count];
        int index = 0;
        if (a != null) {
            nonNulls[index] = a;
            index++;
        }
        if (b != null) {
            nonNulls[index] = b;
            index++;
        }
        if (c != null) {
            nonNulls[index] = c;
        }
        return nonNulls;
    }
    /**
     * Meld the heap with heap2
     * runtime complexity is O(logn)
     */
    public void meld(BinomialHeap heap2) {
        if (heap2.empty())
            return;   // do nothing
        if (this.empty()) { // completely use the other heap
            this.min = heap2.min;
            this.size = heap2.size;
            this.last = heap2.last;
            this.numTrees = heap2.numTrees;
            return;
        }

        HeapNode[] heap1Roots = getRoots();
        HeapNode[] heap2Roots = heap2.getRoots();

        // set the new min
        if (heap2.min.item.key <= this.min.item.key)
            this.min = heap2.min;

        // set the new size
        int newSize = this.size + heap2.size;
        this.size = newSize;

        int newRootsSize = (int) (Math.log(newSize) / Math.log(2)) + 1;

        // pad heap1 roots with nulls
        HeapNode[] heap1RootsPadded = new HeapNode[newRootsSize];
        for (int i = 0; i < heap1Roots.length; i++)
            heap1RootsPadded[i] = heap1Roots[i];
        // pad heap2 roots with nulls
        HeapNode[] heap2RootsPadded = new HeapNode[newRootsSize];
        for (int i = 0; i < heap2Roots.length; i++)
            heap2RootsPadded[i] = heap2Roots[i];
        // create new roots
        HeapNode[] newRoots = new HeapNode[newRootsSize];

        HeapNode carry = null;
        for (int i = 0; i < newRootsSize; i++) {
            HeapNode thisRoot = heap1RootsPadded[i];
            HeapNode otherRoot = heap2RootsPadded[i];
            HeapNode[] notNulls = getNonNulls(thisRoot, otherRoot, carry);
            HeapNode minIfEquals = null;
            if (notNulls.length == 2 || notNulls.length == 3) {
                if (notNulls[0].item.key == notNulls[1].item.key)
                    minIfEquals = notNulls[0] == min ? notNulls[0] : (notNulls[1] == min ? notNulls[1] : null);
            }
            switch (notNulls.length) {
                case 0:
                    break;
                case 1:
                    newRoots[i] = notNulls[0];
                    carry = null;
                    break;
                case 2:
                    // the member of the index i is null by default
                    carry = link(notNulls[0], notNulls[1], minIfEquals);
                    break;
                case 3:
                    carry = link(notNulls[0], notNulls[1], minIfEquals);
                    newRoots[i] = notNulls[2];
                    break;
                default:
            }
        }
        this.last = newRoots[heap1RootsPadded.length - 1];

        HeapNode previous = null;
        HeapNode first = null;
        int newNumTrees = 0;
        for (int i = 0; i < newRoots.length; i++) {
            HeapNode current = newRoots[i];
            if (current != null)
                newNumTrees++; // update numTrees field.
            if (previous == null && current != null) {
                previous = current;
                first = previous;
                continue;
            }
            if (current != null) {
                previous.next = current;
                previous = current;
            }
        }
        previous.next = first;
        numTrees = newNumTrees;
    }

    /**
     * Return the number of elements in the heap
     */
    public int size() {
        return this.size;
    }

    /**
     * The method returns true if and only if the heap
     * is empty.
     */
    public boolean empty() {
        return this.size == 0;
    }

    /**
     * Return the number of trees in the heap.
     * runtime complexity is O(logn)
     */
    public int numTrees() {
        return numTrees;
    }
    /**
     * Class implementing a node in a Binomial Heap.
     */


    public class HeapNode {
        @Override
        public String toString() {
            return String.valueOf(item.key);
        }
        // constructor using item
        public HeapNode(HeapItem item) {
            this.item = item;
            this.parent = null;
            this.rank = 0;
            this.next = this;
            this.child = null;
        }

        // empty constructor
        public HeapNode() {
            this(null);
        }

        public HeapItem item;
        public HeapNode child;
        public HeapNode next;
        public HeapNode parent;
        public int rank;
    }

    /**
     * Class implementing an item in a Binomial Heap.
     */
    public class HeapItem {
        @Override
        public String toString() {
            return String.valueOf(key);
        }

        public HeapItem(int key, String info) {
            this.key = key;
            this.info = info;
        }
        // empty constructor
        public HeapItem(){
            this(0, null);
        }

        public HeapNode node;
        public int key;
        public String info;
    }

}
