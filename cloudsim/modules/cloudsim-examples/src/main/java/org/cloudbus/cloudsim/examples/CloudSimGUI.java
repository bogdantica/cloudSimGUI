package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.crypto.Data;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

public class CloudSimGUI {

    protected static List<Cloudlet> cloudLets;

    protected static List<Vm> virtualMachines;

    protected static List<Datacenter> dataCenters;

    public static void make(String params) {

        Calendar calendar = Calendar.getInstance();

        CloudSim.init(1, calendar, true);

        dataCenters = new ArrayList<Datacenter>();
        cloudLets = new ArrayList<Cloudlet>();
        try {

            DatacenterBroker broker = new DatacenterBroker("Broker");
            List<Host> hostList;// = new ArrayList<Host>();

            params = getFakeParams();

            JSONObject obj = new JSONObject(params);

            JSONArray datacenters = obj.getJSONArray("dataCenters");

            for (int i = 0; i < datacenters.length(); i++) {
                JSONObject dataCenter = datacenters.getJSONObject(i);

                hostList = new ArrayList<>();
                List<Pe> peList = new ArrayList<>();

                JSONArray pes = dataCenter.getJSONArray("pes");
                for (int j = 0; j < pes.length(); j++) {
                    JSONObject pe = pes.getJSONObject(j);
                    peList.add(new Pe(pe.getInt("id"), new PeProvisionerSimple(pe.getInt("mips"))));
                }

                JSONArray hosts = dataCenter.getJSONArray("hosts");

                for (int k = 0; k < hosts.length(); k++) {
                    JSONObject host = hosts.getJSONObject(k);

                    hostList.add(
                            new Host(
                                    host.getInt("id"),
                                    new RamProvisionerSimple(host.getInt("ram")),
                                    new BwProvisionerSimple(host.getInt("bw")),
                                    host.getInt("storage"),
                                    peList,
                                    new VmSchedulerTimeShared(peList)
                            )
                    );
                }

                LinkedList<Storage> storageList = new LinkedList<Storage>();

                JSONObject dcChar = dataCenter.getJSONObject("char");

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


                dataCenters.add(new Datacenter(dataCenter.getString("name"), characteristics, new VmAllocationPolicySimple(hostList), storageList, 0));
                Log.print(dataCenters);
            }

            virtualMachines = new ArrayList<Vm>();


            JSONArray vms = obj.getJSONArray("virtualMachines");

            for (int i = 0; i < vms.length(); i++) {
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

                virtualMachines.add(virtualMachine);
            }
            // submit vm list to the broker

            broker.submitVmList(virtualMachines);


            JSONArray cls = obj.getJSONArray("cloudLets");

            for (int i = 0; i < cls.length(); i++) {
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
//                cloudlet.setVmId(0);

                cloudLets.add(cloudlet);
            }

            broker.submitCloudletList(cloudLets);

            // Sixth step: Starts the simulation
            CloudSim.startSimulation();

            CloudSim.stopSimulation();

            //Final step: Print results when simulation is over
            List<Cloudlet> newList = broker.getCloudletReceivedList();
            printCloudletList(newList);


        } catch (Exception e) {
            Log.printLine(e.getMessage());
            e.printStackTrace();

        }


    }

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


    public static String getFakeParams() {
        return "{\n" +
                "  \"dataCenters\": [\n" +
                "    {\n" +
                "      \"name\": \"Datacenter_1\",\n" +
                "      \"char\": {\n" +
                "        \"arch\": \"x86\",\n" +
                "        \"os\": \"Linux\",\n" +
                "        \"vmm\": \"Xen\",\n" +
                "        \"timeZone\": 10,\n" +
                "        \"cost\": 3,\n" +
                "        \"costPerMem\": 0.05,\n" +
                "        \"costPerStorage\": 0.001,\n" +
                "        \"costPerBw\": 0\n" +
                "      },\n" +
                "      \"pes\": [\n" +
                "        {\n" +
                "          \"id\": 0,\n" +
                "          \"mips\": 2000\n" +
                "        },\n" +
                "        {\n" +
                "          \"id\": 1,\n" +
                "          \"mips\": 2000\n" +
                "        }\n" +

                "      ],\n" +
                "      \"hosts\": [\n" +
                "        {\n" +
                "          \"id\": 0,\n" +
                "          \"ram\": 2048,\n" +
                "          \"storage\": 1000000,\n" +
                "          \"bw\": 10000\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"virtualMachines\": [\n" +
                "    {\n" +
                "      \"id\": 0,\n" +
                "      \"mips\": 1000,\n" +
                "      \"size\": 10000,\n" +
                "      \"ram\": 512,\n" +
                "      \"bw\": 1000,\n" +
                "      \"pesNumber\": 1,\n" +
                "      \"name\": \"Xen\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"mips\": 1000,\n" +
                "      \"size\": 10000,\n" +
                "      \"ram\": 512,\n" +
                "      \"bw\": 1000,\n" +
                "      \"pesNumber\": 1,\n" +
                "      \"name\": \"Xen\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 2,\n" +
                "      \"mips\": 1000,\n" +
                "      \"size\": 10000,\n" +
                "      \"ram\": 512,\n" +
                "      \"bw\": 1000,\n" +
                "      \"pesNumber\": 1,\n" +
                "      \"name\": \"Xen\"\n" +
                "    }\n" +               "  ],\n" +
                "  \"cloudLets\": [\n" +
                "    {\n" +
                "      \"id\": 0,\n" +
                "      \"pesNumber\": 1,\n" +
                "      \"length\": 400000,\n" +
                "      \"fileSize\": 300,\n" +
                "      \"outputSize\": 300\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 1,\n" +
                "      \"pesNumber\": 1,\n" +
                "      \"length\": 200,\n" +
                "      \"fileSize\": 300,\n" +
                "      \"outputSize\": 300\n" +
                "    },\n" +
                "    {\n" +
                "      \"id\": 2,\n" +
                "      \"pesNumber\": 1,\n" +
                "      \"length\": 200000,\n" +
                "      \"fileSize\": 300,\n" +
                "      \"outputSize\": 300\n" +
                "    }\n" +

                "  ]\n" +
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

}
