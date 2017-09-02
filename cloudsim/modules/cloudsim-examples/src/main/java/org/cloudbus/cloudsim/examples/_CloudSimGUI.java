package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

import org.json.*;

import javax.xml.crypto.Data;

public class _CloudSimGUI {

    protected List<Cloudlet> cloudletList;

    protected List<Vm> vmList;

    protected Calendar calendar = Calendar.getInstance();

    protected List<Datacenter> datacenterList;

    protected DatacenterBroker broker;

    public _CloudSimGUI() {

        cloudletList = new ArrayList<>();

        vmList = new ArrayList<>();

        datacenterList = new ArrayList<>();

        CloudSim.init(1, calendar, true);

        String params = getFakeParams();

        parseParams(params);

        CloudSim.startSimulation();

        CloudSim.stopSimulation();

        //Final step: Print results when simulation is over
        List<Cloudlet> newList = broker.getCloudletReceivedList();
        printCloudletList(newList);

        Log.printLine("CloudSimGUI finished!");

    }

//    protected DatacenterCharacteristics characteristics(List<Host> hostList) {
//
//        String arch = "x86"; // system architecture
//        String os = "Linux"; // operating system
//        String vmm = "Xen";
//        double time_zone = 10.0; // time zone this resource located
//        double cost = 3.0; // the cost of using processing in this resource
//        double costPerMem = 0.05; // the cost of using memory in this resource
//        double costPerStorage = 0.001; // the cost of using storage in this
//        // resource
//        double costPerBw = 0.0; // the cost of using bw in this resource
//        // devices by now
//
//        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
//                arch, os, vmm, hostList, time_zone, cost, costPerMem,
//                costPerStorage, costPerBw);
//        return characteristics;
//        try {
//
//            datacenter = new Datacenter("DataCenter-Test", characteristics, new VmAllocationPolicySimple(hostList), new LinkedList<Storage>(), 0);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return datacenter;
////
//    }

//    protected List<Host> hosts() {
//        // Here are the steps needed to create a PowerDatacenter:
//        // 1. We need to create a list to store
//        // our machine
//        List<Host> hostList = new ArrayList<Host>();
//
//        // 2. A Machine contains one or more PEs or CPUs/Cores.
//        // In this example, it will have only one core.
//        List<Pe> peList = new ArrayList<Pe>();
//
//        int mips = 1000;
//
//        // 3. Create PEs and add these into a list.
//        peList.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
//
//        // 4. Create Host with its id and list of PEs and add them to the list
//        // of machines
//        int hostId = 0;
//        int ram = 2048; // host memory (MB)
//        long storage = 1000000; // host storage
//        int bw = 10000;
//
//        hostList.add(
//                new Host(
//                        hostId,
//                        new RamProvisionerSimple(ram),
//                        new BwProvisionerSimple(bw),
//                        storage,
//                        peList,
//                        new VmSchedulerTimeShared(peList)
//                )
//        ); // This is our machine
//
//        return hostList;
//    }

    private static void printCloudletList(List<Cloudlet> list) {
        int size = list.size();

        Cloudlet cloudlet;

        String indent = "    ";
        Log.printLine();
        Log.printLine("========== OUTPUT ==========");
        Log.printLine("Cloudlet ID" + indent + "STATUS" + indent
                + "Data center ID" + indent + "VM ID" + indent + "Time" + indent
                + "Start Time" + indent + "Finish Time");

        DecimalFormat dft = new DecimalFormat("###.##");
        for (int i = 0; i < size; i++) {
            cloudlet = list.get(i);
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS) {
                Log.print("SUCCESS");

                Log.printLine(indent + indent + cloudlet.getResourceId()
                        + indent + indent + indent + cloudlet.getVmId()
                        + indent + indent
                        + dft.format(cloudlet.getActualCPUTime()) + indent
                        + indent + dft.format(cloudlet.getExecStartTime())
                        + indent + indent
                        + dft.format(cloudlet.getFinishTime()));
            }
        }
    }


    public void parseParams(String params) {

        JSONObject obj = null;

        try {
            obj = new JSONObject(params);

            parseDatacenters(obj.getJSONArray("dataCenters"));

            createBroker("Broker");

            parseVirtualMachines(obj.getJSONArray("virtualMachines"));

            parseCloudLets(obj.getJSONArray("cloudLets"));

        } catch (JSONException e) {
            Log.printLine(e);
        }
    }

    public void parseCloudLets(JSONArray cls) {
        for (int i = 0; i < cls.length(); i++) {
            try {
                JSONObject cl = cls.getJSONObject(i);

                UtilizationModel utilizationModel = new UtilizationModelFull();

                Cloudlet cloudlet =
                        new Cloudlet(
                                cl.getInt("id"),
                                cl.getInt("length"),
                                cl.getInt("pesNumber"),
                                cl.getLong("fileSize"),
                                cl.getLong("outputSize"),
                                utilizationModel,
                                utilizationModel,
                                utilizationModel
                        );

                cloudlet.setUserId(broker.getId());
                cloudlet.setVmId(cl.getInt("id"));

                cloudletList.add(cloudlet);

                broker.submitCloudletList(cloudletList);

            } catch (JSONException e) {
                Log.printLine(e);
            }

        }
    }

    public void parseVirtualMachines(JSONArray vms) {
        for (int i = 0; i < vms.length(); i++) {
            try {
                JSONObject vm = vms.getJSONObject(i);
                // create VM
                Vm virtualMachine = new Vm(
                        vm.getInt("id"),
                        broker.getId(),
                        vm.getInt("mips"),
                        vm.getInt("pesNumber"),
                        vm.getInt("ram"),
                        vm.getInt("bw"),
                        vm.getInt("size"),
                        vm.getString("name"),
                        new CloudletSchedulerTimeShared()
                );

                vmList.add(virtualMachine);

            } catch (JSONException e) {
                Log.printLine(e);
            }

        }
        // submit vm list to the broker

        broker.submitVmList(vmList);

    }

    public void parseDatacenters(JSONArray dcs) {
        for (int i = 0; i < dcs.length(); i++) {
            try {
                JSONObject dataCenter = dcs.getJSONObject(i);

                datacenterList.add(createDatacenter(dataCenter));

            } catch (JSONException e) {
                Log.printLine(e);
            }
        }
    }


    public String getFakeParams() {
        return "{" +
                "  \"dataCenters\": [" +
                "    {" +
                "      \"name\": \"Datacenter_1\"," +
                "      \"char\": {" +
                "        \"arch\": \"x86\"," +
                "        \"os\": \"Linux\"," +
                "        \"vmm\": \"Xen\"," +
                "        \"timeZone\": 10," +
                "        \"cost\": 3," +
                "        \"costPerMem\": 0.05," +
                "        \"costPerStorage\": 0.001," +
                "        \"costPerBw\": 0" +
                "      }," +
                "      \"pes\": [" +
                "        {" +
                "          \"id\": 0," +
                "          \"mips\": 1000" +
                "        }" +
                "      ]," +
                "      \"hosts\": [" +
                "        {" +
                "          \"id\": 0," +
                "          \"ram\": 2048," +
                "          \"storage\": 1000000," +
                "          \"bw\": 10000" +
                "        }" +
                "      ]" +
                "    }," +
                "    {" +
                "      \"name\": \"Datacenter_2\"," +
                "      \"char\": {" +
                "        \"arch\": \"x86\"," +
                "        \"os\": \"Linux\"," +
                "        \"vmm\": \"Xen\"," +
                "        \"timeZone\": 10," +
                "        \"cost\": 3," +
                "        \"costPerMem\": 0.05," +
                "        \"costPerStorage\": 0.001," +
                "        \"costPerBw\": 0" +
                "      }," +
                "      \"pes\": [" +
                "        {" +
                "          \"id\": 0," +
                "          \"mips\": 1000" +
                "        }" +
                "      ]," +
                "      \"hosts\": [" +
                "        {" +
                "          \"id\": 0," +
                "          \"ram\": 2048," +
                "          \"storage\": 1000000," +
                "          \"bw\": 10000" +
                "        }" +
                "      ]" +
                "    }" +
                "  ]," +
                "  \"virtualMachines\": [" +
                "    {" +
                "      \"id\": 0," +
                "      \"mips\": 1000," +
                "      \"size\": 10000," +
                "      \"ram\": 512," +
                "      \"bw\": 1000," +
                "      \"pesNumber\": 1," +
                "      \"name\": \"Xen\"" +
                "    }" +
                "  ]," +
                "  \"cloudLets\": [" +
                "    {" +
                "      \"id\": 0," +
                "      \"pesNumber\": 1," +
                "      \"length\": 400000," +
                "      \"fileSize\": 300," +
                "      \"outputSize\": 300" +
                "    }" +
                "  ]" +
                "}";
    }


    public static Datacenter createDatacenter(JSONObject dc) {

        List<Host> hostList = new ArrayList<>();
        List<Pe> peList = new ArrayList<>();

        Datacenter datacenter = null;

        try {
            JSONArray pes = dc.getJSONArray("pes");
            for (int i = 0; i < pes.length(); i++) {
                JSONObject pe = pes.getJSONObject(i);
                peList.add(new Pe(pe.getInt("id"), new PeProvisionerSimple(pe.getInt("mips"))));
            }

            JSONArray hosts = dc.getJSONArray("hosts");

            for (int i = 0; i < hosts.length(); i++) {
                JSONObject host = hosts.getJSONObject(i);

                hostList.add(
                        new Host(
                                host.getInt("id"),
                                new RamProvisionerSimple(host.getInt("id")),
                                new BwProvisionerSimple(host.getInt("bw")),
                                host.getInt("storage"),
                                peList,
                                new VmSchedulerTimeShared(peList)
                        )
                );
            }


            LinkedList<Storage> storageList = new LinkedList<Storage>();

            JSONObject dcChar = dc.getJSONObject("char");

            DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                    dcChar.getString("arch"),
                    dcChar.getString("os"),
                    dcChar.getString("vmm"),
                    hostList,
                    dcChar.getDouble("timeZone"),
                    dcChar.getDouble("cost"),
                    dcChar.getDouble("costPerMem"),
                    dcChar.getDouble("costPerStorage"),
                    dcChar.getDouble("costPerBw")
            );


            datacenter = new Datacenter(dc.getString("name"), characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);

            return datacenter;

        } catch (JSONException e) {
            Log.printLine(e);

        } catch (Exception e) {
            Log.printLine("Here 1");
            Log.printLine(e);
        }

        return datacenter;
    }

    public DatacenterBroker createBroker(String name) {

        try {
            broker = new DatacenterBroker(name);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return broker;
    }
}
