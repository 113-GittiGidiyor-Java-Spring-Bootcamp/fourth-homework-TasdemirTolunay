package dev.patika.schoolsystem.service;

import dev.patika.schoolsystem.dto.AddressDTO;
import dev.patika.schoolsystem.dto.CourseDTO;
import dev.patika.schoolsystem.dto.StudentDTO;
import dev.patika.schoolsystem.dto.StudentWithCoursesDTO;
import dev.patika.schoolsystem.entity.Address;
import dev.patika.schoolsystem.entity.Course;
import dev.patika.schoolsystem.entity.Student;
import dev.patika.schoolsystem.exceptions.EmptyListException;
import dev.patika.schoolsystem.exceptions.IdNotFoundException;
import dev.patika.schoolsystem.exceptions.StudentAgeNotValidException;
import dev.patika.schoolsystem.mapper.AddressMapper;
import dev.patika.schoolsystem.mapper.CourseMapper;
import dev.patika.schoolsystem.mapper.StudentMapper;
import dev.patika.schoolsystem.mapper.StudentWithCoursesMapper;
import dev.patika.schoolsystem.repository.AddressRepository;
import dev.patika.schoolsystem.repository.CourseRepository;
import dev.patika.schoolsystem.repository.StudentRepository;
import dev.patika.schoolsystem.util.ErrorMessageConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentWithCoursesMapper studentWithCoursesMapper;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private AddressMapper addressMapper;


    public List<StudentWithCoursesDTO> findAllStudents(){

        List<Student> studentList = new ArrayList<>();
        Iterable<Student> studentIterable = studentRepository.findAll();
        studentIterable.iterator().forEachRemaining(studentList :: add);
        return studentWithCoursesMapper.mapStudentListToStudentWithCoursesDTOList(studentList);

    }

    public StudentWithCoursesDTO findByStudentId(long studentId){

        Student foundStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new IdNotFoundException(String.format(ErrorMessageConstants.STUDENT_NOT_FOUND, studentId)));
        return studentWithCoursesMapper.mapStudentToStudentWithCoursesDTO(foundStudent);

    }

    @Transactional
    public StudentWithCoursesDTO saveStudent(StudentDTO studentDTO){

        Student foundStudent = studentMapper.mapStudentDTOToStudent(studentDTO);
        int birthDateYear = foundStudent.getStudentBirthDate().getYear();
        int age = (LocalDate.now().getYear()) - birthDateYear;
        if(age < 18 || age > 40){
            throw new StudentAgeNotValidException(ErrorMessageConstants.WRONG_AGE);
        }
        return studentWithCoursesMapper.mapStudentToStudentWithCoursesDTO(studentRepository.save(foundStudent));

    }

    @Transactional
    public StudentWithCoursesDTO updateStudent(StudentDTO studentDTO, long studentId){

        Student foundStudent = studentRepository.findById(studentId)
                .orElseThrow(() -> new IdNotFoundException(String.format(ErrorMessageConstants.STUDENT_NOT_FOUND, studentId)));
        foundStudent.setStudentName(studentDTO.getStudentName());
        foundStudent.setStudentGender(studentDTO.getStudentGender());
        foundStudent.setStudentBirthDate(studentDTO.getStudentBirthDate());
        int birthDateYear = foundStudent.getStudentBirthDate().getYear();
        int age = (LocalDate.now().getYear()) - birthDateYear;
        if(age < 18 || age > 40){
            throw new StudentAgeNotValidException(ErrorMessageConstants.WRONG_AGE);
        }
        studentRepository.save(foundStudent);
        return studentWithCoursesMapper.mapStudentToStudentWithCoursesDTO(foundStudent);

    }

    @Transactional
    public String deleteStudentById(long studentId){

        Student foundStudent = studentRepository.findById(studentId).get();
        List<Course> courseList = foundStudent.getCourses();
        for (Course c : courseList) {

            c.getStudents().remove(foundStudent);
            courseRepository.save(c);
        }
        studentRepository.deleteById(studentId);
        return "Student with id = " + studentId + " Deleted....";

    }

    public List<StudentWithCoursesDTO> findStudentByName(String studentName){

        List<Student> studentList = studentRepository.findStudentByStudentName(studentName);
        return studentWithCoursesMapper.mapStudentListToStudentWithCoursesDTOList(studentList);

    }

    public List<?> genderGroups(){

        return studentRepository.getGenderWithGrouping();

    }

    public List<CourseDTO> findCoursesOfStudent(long studentId){

        List<Course> courseList = studentRepository.findById(studentId).get().getCourses();
        return courseMapper.mapCourseListToCourseDTOList(courseList);

    }

    public AddressDTO findAddressOfStudent(long studentId){

        Address address = studentRepository.findById(studentId).get().getStudentAddress();
        return addressMapper.mapAddressToAddressDTO(address);

    }

    public Address findAddressById(long addressId){

        Address foundAddress = addressRepository.findById(addressId)
                .orElseThrow(() -> new IdNotFoundException(String.format(ErrorMessageConstants.ADDRESS_NOT_FOUND, addressId)));

        return foundAddress;

    }

    public Student findStudentById(long studentId){

        return studentRepository.findById(studentId)
                .orElseThrow(() -> new IdNotFoundException(String.format(ErrorMessageConstants.STUDENT_NOT_FOUND, studentId)));

    }

    public List<String> StudentsCourses(long studentId){

        List<String> courseNames = new ArrayList<>();
        List<Course> foundStudentsCourses = findStudentById(studentId).getCourses();
        for (Course c : foundStudentsCourses) {

            courseNames.add(c.getCourseName());

        }

        return courseNames;

    }

    public String  studentsAddressCity(long studentId){

        return findStudentById(studentId).getStudentAddress().getCity();

    }

}
